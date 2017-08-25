package ch.frankel.blog.migrate.processor

import ch.frankel.blog.migrate.BlogEntry
import ch.frankel.blog.migrate.Status.DRAFT
import ch.frankel.blog.migrate.Status.PUBLISH
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.item.*

abstract class DiscardEntryItemProcessor : ItemProcessor<BlogEntry, BlogEntry> {

    internal abstract val predicate: (BlogEntry) -> Boolean
    internal abstract val logger: Logger

    override fun process(entry: BlogEntry) = when (predicate(entry)) {
        true -> entry
        else -> {
            logger.info("Discarding entry ${entry.name}")
            null
        }
    }
}


class DiscardTrashEntryItemProcessor : DiscardEntryItemProcessor() {

    override val predicate: (BlogEntry) -> Boolean
        get() = { e -> e.status == PUBLISH || e.status == DRAFT }
    override val logger = LoggerFactory.getLogger(DiscardTrashEntryItemProcessor::class.java)
}

class DiscardEmptyTitleEntryItemProcessor : DiscardEntryItemProcessor() {

    override val predicate: (BlogEntry) -> Boolean
        get() = { e -> e.title.isNotEmpty() }
    override var logger = LoggerFactory.getLogger(DiscardEmptyTitleEntryItemProcessor::class.java)
}


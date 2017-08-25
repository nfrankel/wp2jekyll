package ch.frankel.blog.migrate

import ch.frankel.blog.migrate.processor.*
import org.springframework.batch.core.configuration.annotation.*
import org.springframework.batch.item.support.*
import org.springframework.beans.factory.annotation.*
import org.springframework.context.annotation.*

@Configuration
open class BatchConfiguration {

    @Autowired
    private lateinit var jobs: JobBuilderFactory

    @Autowired
    private lateinit var steps: StepBuilderFactory

    @Autowired
    private lateinit var reader: BlogEntryItemReader

    @Autowired
    private lateinit var writer: BlogEntryItemWriter

    @Autowired
    private lateinit var addImageSizeContentItemProcessor: AddImageSizeContentItemProcessor

    @Bean
    open fun job() = jobs.get("job").start(step()).build()

    @Bean
    open protected fun step() = steps.get("step").chunk<BlogEntry, BlogEntry>(10)
            .reader(reader)
            .processor(CompositeItemProcessor<BlogEntry, BlogEntry>().apply {
                setDelegates(listOf(DiscardTrashEntryItemProcessor(),
                        DiscardEmptyTitleEntryItemProcessor(),
                        RemoveExtraLineBreaksItemProcessor(),
                        ReplaceAbsoluteLinkByRelativeItemProcessor(),
                        FixResourcePathItemProcessor(),
                        ReplaceCodeTagsContentItemProcessor(),
                        RemoveExtraPreTagItemProcessor(),
                        UnescapeHtmlEntitiesItemProcessor(),
                        ReplaceGistsLinkItemProcessor(),
                        addImageSizeContentItemProcessor,
                        ReplaceHeadingWithBiggerHeadingItemProcessor(),
                        RemoveMainImageItemProcessor()))
            })
            .writer(writer).build()
}
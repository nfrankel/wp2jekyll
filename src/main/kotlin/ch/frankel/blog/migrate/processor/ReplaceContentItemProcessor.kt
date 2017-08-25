package ch.frankel.blog.migrate.processor

import ch.frankel.blog.migrate.BlogEntry
import org.jsoup.parser.Parser
import org.springframework.batch.item.*

class UnescapeHtmlEntitiesItemProcessor : ItemProcessor<BlogEntry, BlogEntry> {
    override fun process(entry: BlogEntry) = entry.copy(content = Parser.unescapeEntities(entry.content, false))
}

class ReplaceHeadingWithBiggerHeadingItemProcessor : ItemProcessor<BlogEntry, BlogEntry> {
    override fun process(entry: BlogEntry) = entry.copy(content = entry.content.replace("h3>", "h2>").replace("h4>", "h3>"))
}

abstract class ReplaceRegexItemProcessor(val regex: Regex, val replacement: String) : ItemProcessor<BlogEntry, BlogEntry> {
    override fun process(entry: BlogEntry) = entry.copy(content = entry.content.replace(regex, replacement))
}

class FixResourcePathItemProcessor : ReplaceRegexItemProcessor(Regex("/wp-content/(resources|images)"), "/assets/resources")

class ReplaceGistsLinkItemProcessor : ReplaceRegexItemProcessor(Regex("(\n)https://gist.github.com/(.*)"), "$1{% gist $2 %}")

class ReplaceAbsoluteLinkByRelativeItemProcessor : ReplaceRegexItemProcessor(Regex("(href|src)=\"https?://blog.frankel.ch/"), "$1=\"/")

private var lineBreak = System.getProperty("line.separator")

class RemoveExtraLineBreaksItemProcessor : ReplaceRegexItemProcessor(Regex("${lineBreak}{3,}"), "${lineBreak}${lineBreak}")



package ch.frankel.blog.migrate.processor

import ch.frankel.blog.migrate.BlogEntry
import org.jsoup.parser.Parser
import org.springframework.batch.item.*

class RemoveExtraPreTagItemProcessor : ItemProcessor<BlogEntry, BlogEntry> {
    override fun process(entry: BlogEntry) = entry.copy(
            content = entry.content.replace("<pre>{% highlight", "{% highlight").replace("endhighlight %}</pre>", "endhighlight %}"))
}

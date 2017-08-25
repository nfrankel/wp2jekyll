package ch.frankel.blog.migrate.processor

import ch.frankel.blog.migrate.BlogEntry
import org.springframework.batch.item.*

class ReplaceCodeTagsContentItemProcessor : ItemProcessor<BlogEntry, BlogEntry> {

    override fun process(entry: BlogEntry) = entry.copy(
            content = entry.content.replace("[java]", "{% highlight java %}")
                    .replace("[/java]", "{% endhighlight %}")
                    .replace("[xml]", "{% highlight xml %}")
                    .replace("[/xml]", "{% endhighlight %}")
                    .replace("[html]", "{% highlight html %}")
                    .replace("[/html]", "{% endhighlight %}")
                    .replace("[javascript]", "{% highlight javascript %}")
                    .replace("[/javascript]", "{% endhighlight %}")
                    .replace("[bash]", "{% highlight bash %}")
                    .replace("[/bash]", "{% endhighlight %}")
                    .replace("[scala]", "{% highlight scala %}")
                    .replace("[/scala]", "{% endhighlight %}")
                    .replace("[groovy]", "{% highlight groovy %}")
                    .replace("[/groovy]", "{% endhighlight %}")
                    .replace("[text]", "{% highlight text %}")
                    .replace("[/text]", "{% endhighlight %}")
                    .replace("[code]", "{% highlight text %}")
                    .replace("[/code]", "{% endhighlight %}"))
}


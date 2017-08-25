package ch.frankel.blog.migrate

import com.thoughtworks.xstream.core.DefaultConverterLookup
import org.springframework.batch.core.configuration.annotation.*
import org.springframework.batch.item.xml.*
import org.springframework.beans.factory.annotation.*
import org.springframework.core.io.*
import org.springframework.oxm.xstream.*
import org.springframework.stereotype.*

@Component @StepScope
open class BlogEntryItemReader(@Value("file:#{jobParameters['source']}") var source: Resource) : StaxEventItemReader<BlogEntry>() {

    init {
        setFragmentRootElementName("item")
        setResource(source)
        setUnmarshaller(XStreamMarshaller().apply {
            setConverterLookup(DefaultConverterLookup().apply {
                registerConverter(BlogEntryConverter(), 1)
            })
            setAliases(mapOf("item" to BlogEntry::class.java))
        })
    }
}
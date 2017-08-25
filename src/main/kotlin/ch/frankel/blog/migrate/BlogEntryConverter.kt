package ch.frankel.blog.migrate

import com.thoughtworks.xstream.converters.Converter
import com.thoughtworks.xstream.converters.MarshallingContext
import com.thoughtworks.xstream.converters.UnmarshallingContext
import com.thoughtworks.xstream.io.HierarchicalStreamReader
import com.thoughtworks.xstream.io.HierarchicalStreamWriter
import java.text.SimpleDateFormat
import java.util.*
import java.util.Locale.*

class BlogEntryConverter() : Converter {

    private var blogPublishFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", ENGLISH)
    private var commentFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", ENGLISH)

    override fun marshal(source: Any, writer: HierarchicalStreamWriter, context: MarshallingContext) = throw UnsupportedOperationException()

    override fun unmarshal(reader: HierarchicalStreamReader, context: UnmarshallingContext): BlogEntry {
        var title = ""
        var name = ""
        var publishedDate = Date()
        var content = ""
        var type = Type.PAGE
        var status = Status.DRAFT
        val tags = arrayListOf<String>()
        var category = ""
        var excerpt = ""
        var i = 0
        val comments = arrayListOf<Comment>()
        while (reader.hasMoreChildren()) {
            reader.moveDown()
            when {
                reader.nodeName == "title" -> title = reader.value
                reader.nodeName == "post_name" -> name = reader.value
            // There are 2 encoded attributes in 2 different namespaces
                reader.nodeName == "encoded" && i == 0 -> {
                    i = 1
                    content = reader.value
                }
                reader.nodeName == "encoded" && i == 1 -> {
                    i = 0
                    excerpt = reader.value
                }
                reader.nodeName == "pubDate" -> publishedDate = blogPublishFormat.parse(reader.value)
                reader.nodeName == "post_type" -> if (reader.value == "post") type = Type.POST
                reader.nodeName == "status" && reader.value == "publish" -> status = Status.PUBLISH
                reader.nodeName == "status" && reader.value == "trash" -> status = Status.TRASH
                reader.nodeName == "status" && reader.value == "draft" -> status = Status.DRAFT
                reader.nodeName == "category" && reader.getAttribute("domain") == "category" -> category = reader.value
                reader.nodeName == "category" && reader.getAttribute("domain") == "post_tag" -> tags.add(reader.value)
                reader.nodeName == "comment" -> {
                    comments.add(Comment())
                    while (reader.hasMoreChildren()) {
                        reader.moveDown()
                        when {
                            reader.nodeName == "comment_author" -> comments.last().author.name = reader.value
                            reader.nodeName == "comment_author_email" -> comments.last().author.email = reader.value
                            reader.nodeName == "comment_author_url" -> comments.last().author.url = reader.value
                            reader.nodeName == "comment_content" -> comments.last().content = reader.value
                            reader.nodeName == "comment_date" -> comments.last().date = commentFormat.parse(reader.value)
                        }
                        reader.moveUp()
                    }
                }
            }
            reader.moveUp()
        }
        return when (comments.isEmpty()) {
            true -> BlogEntry(title, name, publishedDate, content, tags, category, type, status, excerpt)
            else -> BlogEntry(title, name, publishedDate, content, tags, category, type, status, excerpt, comments = comments)
        }
    }

    override fun canConvert(type: Class<*>?) = type == BlogEntry::class.java
}
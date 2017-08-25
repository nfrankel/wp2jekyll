package ch.frankel.blog.migrate

import org.springframework.batch.core.configuration.annotation.*
import org.springframework.batch.item.*
import org.springframework.beans.factory.annotation.*
import org.springframework.core.io.*
import org.springframework.stereotype.*
import java.io.File
import java.io.PrintWriter
import java.text.SimpleDateFormat

@Component @StepScope
open class BlogEntryItemWriter(@Value("file:#{jobParameters['target']}") var target: Resource) : ItemWriter<BlogEntry> {

    private val format = SimpleDateFormat("yyyy-MM-dd")

    override fun write(entries: MutableList<out BlogEntry>) {
        entries.forEach { it.write() }
    }

    private val BlogEntry.jekyllFileName: String
        get() = when {
            type == Type.PAGE || status == Status.DRAFT -> "$name.md"
            status == Status.PUBLISH -> "${format.format(publishDate)}-$name.md"
            else -> throw RuntimeException("Impossible combination for entry $name")
        }

    private val BlogEntry.jekyllType: String
        get() = when (status) {
            Status.PUBLISH -> "posts"
            Status.DRAFT -> "drafts"
            else -> throw RuntimeException("No status available for entry $name")
    }

    private val BlogEntry.jekyllLayout: String
        get() = when (type) {
            Type.POST -> "post"
            Type.PAGE -> "page"
        }

    private val BlogEntry.jekyllFolder: File
        get() = when(type) {
            Type.POST -> File("${target.file.absolutePath}/_$jekyllType")
            Type.PAGE -> File(target.file.absolutePath)
        }

    private fun BlogEntry.write() {
        val file = File(jekyllFolder, jekyllFileName)
        file.createNewFile()
        val writer = PrintWriter(file)
        fun writeHeader() {
            with(writer) {
                println("---")
                println("layout: $jekyllLayout")
                println("type: $jekyllType")
                println("title: \"$title\"")
                if (tags.isNotEmpty()) {
                    print("tags: [")
                    print(tags.joinToString())
                    println("]")
                }
                if (category.isNotBlank()) println("categories: $category")
                if (excerpt.isNotBlank()) println("excerpt: $excerpt")
                picture?.let {
                    println("picture:")
                    println("    src: ${it.src}")
                    it.height?.let { println("    height: ${it}") }
                    it.width?.let {  println("    width: ${it}") }
                    it.link?.let {   println("    link: ${it}") }
                    it.alt?.let {    if (it.isNotEmpty()) println("    alt: \"${it}\"") }
                }
                if (comments.isNotEmpty()) {
                    println("comments:")
                    comments.forEach {
                        println("    -")
                        println("        author:")
                        println("            name: \"${it.author.name}\"")
                        println("            url: ${it.author.url}")
                        println("            email: ${it.author.email}")
                        println("        date: ${format.format(it.date)}")
                        println("        content: \"${it.content.replace("\"", "\\\"")}\"")
                    }
                }
                println("---")
                println()
            }
        }
        writeHeader()
        writer.write(content)
        writer.close()
    }
}

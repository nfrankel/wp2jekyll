package ch.frankel.blog.migrate.processor

import ch.frankel.blog.migrate.BlogEntry
import org.slf4j.LoggerFactory
import org.springframework.batch.core.configuration.annotation.*
import org.springframework.batch.item.*
import org.springframework.beans.factory.annotation.*
import org.springframework.core.io.*
import org.springframework.stereotype.*
import java.io.FileInputStream
import java.io.InputStream
import java.net.URL
import javax.imageio.*

@Component @StepScope
open class AddImageSizeContentItemProcessor(@Value("file:#{jobParameters['target']}") var target: Resource) : ItemProcessor<BlogEntry, BlogEntry> {

    private val maxImageWidth = 1000
    private val logger = LoggerFactory.getLogger(AddImageSizeContentItemProcessor::class.java)

    override fun process(entry: BlogEntry): BlogEntry {
        val images = entry.imageTags().filter { !it.contains("height=") || !it.contains("width=") }
        if (images.isNotEmpty()) {
            logger.info("Activating Image Processor because images are missing resources ")
        }
        images.map { it to it.addSizeAttributes() }
                .forEach {
                    entry.content = entry.content.replace(it.first, it.second)
                }
        return entry
    }

    private fun BlogEntry.imageTags(): List<String> {

        fun String.findNextImageStart(position: Int) = indexOf("<img", position)

        val imageTags = mutableListOf<String>()
        var cursor = 0
        with(content) {
            while (findNextImageStart(cursor) != -1) {
                val start = findNextImageStart(cursor)
                val end = indexOf(">", start) + 1
                val slice = substring(start, end)
                cursor = end
                imageTags.add(slice)
            }
            return imageTags
        }
    }

    fun String.addSizeAttributes(): String {

        fun String.withoutClosingTags(): String {
            return when {
                endsWith("/>") -> dropLast(2)
                else -> dropLast(1)
            }
        }

        fun ImageReader.size(): Pair<Int, Int> {
            val height = getHeight(0)
            val width = getWidth(0)
            return when {
                (width > maxImageWidth) -> height * maxImageWidth / width to maxImageWidth
                else -> height to width
            }
        }

        val start = indexOf("src=\"")
        if (start == -1) {
            logger.error("Unable to find src attribute for ${this}")
            return this
        }
        val end = indexOf("\"", start + 5)
        val src = substring(start + 5, end)
        var stream: InputStream? = null
        try {
            stream = when {
                src.startsWith("/") -> FileInputStream(src.replaceFirst("/", "${target.file.absolutePath}/"))
                else -> URL(src).openStream()
            }
            val obj = ImageIO.createImageInputStream(stream)
            val reader = ImageIO.getImageReaders(obj).next()
            reader.input = obj // Stupid, but mandatory
            return "${withoutClosingTags()} height=\"${reader.size().first}\" width=\"${reader.size().second}\" />"
        } catch (e: Exception) {
            logger.error("$src: ${e.message}")
            return this
        } finally {
            stream?.close();
        }
    }
}
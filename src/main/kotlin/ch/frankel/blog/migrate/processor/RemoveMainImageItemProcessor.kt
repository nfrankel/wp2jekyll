package ch.frankel.blog.migrate.processor

import ch.frankel.blog.migrate.BlogEntry
import ch.frankel.blog.migrate.Picture
import org.springframework.batch.item.*
import java.io.FileInputStream
import java.io.InputStream
import java.net.URL
import javax.imageio.*

class RemoveMainImageItemProcessor : ItemProcessor<BlogEntry, BlogEntry> {

    override fun process(entry: BlogEntry): BlogEntry {
        val content = entry.content
        return when {
            content.startsWith("<img") -> {
                val endImg = content.indexOf("/>") + 2
                val imgString = content.substring(0, endImg)
                val pic = processImage(imgString)
                entry.copy(picture = pic, content = entry.content.substring(endImg))
            }
            content.startsWith("<a ") -> {
                val endA = content.indexOf("</a>") + 2
                val imgString = content.substring(0, endA)
                if (!imgString.contains("<img")) {
                    return entry
                }
                val pic = processLinkedImage(imgString)
                entry.copy(picture = pic, content = entry.content.substring(endA + 2))
            }
            else -> entry
        }
    }

    private fun String.getAttribute(name: String): String? {
        val start = indexOf(name)
        return when (start != - 1) {
            true -> {
                val realStart = start + name.length + 2
                val end = indexOf("\"", realStart)
                return substring(realStart , end)
            }
            else -> null
        }
    }

    private fun processLinkedImage(string: String): Picture {
        val href = string.getAttribute("href")
        val picture = processImage(string)
        return picture.copy(link = href)
    }

    private fun processImage(string: String): Picture {

        val src = string.getAttribute("src")
        val alt = string.getAttribute("alt")
        val height = string.getAttribute("height")
        val width = string.getAttribute("width")
        val size = src!!.computeImageSize()
        val realHeight = size?.component1()
        val realWidth = size?.component2()
        return Picture(src, height?.toInt() ?: realHeight, width?.toInt() ?: realWidth, alt)
    }

    private fun String.computeImageSize(): Pair<Int, Int>? {
        var stream: InputStream? = null
        try {
            stream = when {
                startsWith("/") -> FileInputStream(this)
                else -> URL(this).openStream()
            }
            val obj = ImageIO.createImageInputStream(stream)
            val reader = ImageIO.getImageReaders(obj).next()
            reader.input = obj // Stupid, but mandatory
            return reader.getWidth(0) to reader.getHeight(0)
        } catch (e: Exception) {
            return null
        } finally {
            stream?.close();
        }
    }
}
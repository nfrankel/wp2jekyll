package ch.frankel.blog.migrate

import java.util.*

data class BlogEntry(var title: String,
                     var name: String,
                     var publishDate: Date,
                     var content: String,
                     var tags: List<String>,
                     var category: String,
                     var type: Type,
                     var status: Status,
                     var excerpt: String,
                     var picture: Picture? = null,
                     var comments: List<Comment> = ArrayList<Comment>())

enum class Type {
    POST, PAGE
}

enum class Status {
    PUBLISH, DRAFT, TRASH
}

data class Picture(var src: String, var height: Int?, var width: Int?, var alt: String?, var link: String? = null)

data class Comment(var content: String = "", var date: Date = Date(), var author: Author = Author())

data class Author(var name: String = "", var email: String = "", var url: String = "")
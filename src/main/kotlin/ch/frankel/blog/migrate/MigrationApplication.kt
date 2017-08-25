package ch.frankel.blog.migrate

import org.springframework.batch.core.configuration.annotation.*
import org.springframework.boot.*
import org.springframework.boot.autoconfigure.*

@SpringBootApplication
@EnableBatchProcessing
open class MigrationApplication

fun main(args: Array<String>) {
    System.exit(SpringApplication.exit(SpringApplication.run(MigrationApplication::class.java, *args)))
}

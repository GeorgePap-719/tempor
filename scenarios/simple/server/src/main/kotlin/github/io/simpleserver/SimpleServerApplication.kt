package github.io.simpleserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SimpleServerApplication

fun main(args: Array<String>) {
  runApplication<SimpleServerApplication>(*args)
}

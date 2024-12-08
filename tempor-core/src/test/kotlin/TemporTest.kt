package tempor

import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class TemporTest {

  @Test
  fun basicScenario() = runBlocking {
    val engine = Tempor(handlers = handlers)
    engine.emit("Hello world ")
    engine.emit("Hello world 2")
    Thread.sleep(200)
    engine.emit("Hello world 3")
    engine.cancelAndJoin(2.seconds)
  }
}

val printHandler = EventHandler<String> {
  println(it)
}
val mapHandler = EventHandler<String> {
  println((it + it))
}

val handlers = listOf(printHandler, mapHandler)
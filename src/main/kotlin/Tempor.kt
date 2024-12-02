package tempor

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ChannelResult
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.getOrElse
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

// High level design:
// 1 - simple API: for adding events, registering eventHandlers
// 2 - use channels under the hood to process events
// 3 - let's set aside for now the builder around this.
class Tempor<T>(
  private val handlers: List<EventHandler<T>>,
  private val dispatcher: CoroutineDispatcher = Dispatchers.Default.limitedParallelism(1, "temporDispatcher")
) {
  private val store = Channel<T>(100_000)
  private val monitor = Channel<Unit>(0, BufferOverflow.DROP_LATEST)

  private val monitorJob: Job

  init {
    monitorJob = initMonitor()
  }

  private fun initMonitor(): Job = CoroutineScope(dispatcher).launch {
    //monitor.consumeAsFlow().conflate().flowOn(dispatcher)
    monitor.consumeAsFlow().collect {
      println("-- COLLECTING -- ")
      while (true) {
        val element = store.tryReceive().getOrElse { return@collect }
        for (handler in handlers) {
          launch { handler.handle(element) }
        }
      }
    }
  }

  fun cancelAndJoin(duration: Duration) = runBlocking {
    delay(duration)
    // Let's add some clean up operations for easier testing.
//    store.close()
    monitorJob.cancelAndJoin()
  }

  fun tryAdd(value: T): ChannelResult<Unit> {
    val result = store.trySend(value)
    monitor.trySend(Unit) // ignore failures
    return result
  }
}

fun interface EventHandler<T> {
  fun handle(value: T)
}

val printHandler = EventHandler<String> {
  println(it)
}
val mapHandler = EventHandler<String> {
  println((it + it)) // result will be ignored anyway
}

val handlers = listOf(printHandler, mapHandler)

fun main() {
  val engine = Tempor(handlers = handlers)
  val result = engine.tryAdd("Hello world ")
  println(result.isSuccess)
  engine.cancelAndJoin(2.seconds)
}

@OptIn(ExperimentalCoroutinesApi::class)
fun CoroutineScope.fixedPeriodTicker(
  delayMillis: Long,
): ReceiveChannel<Unit> {
  return produce(capacity = 0) {
    // We don't really need initial delay.
    // delay(delayMillis)
    while (true) {
      channel.send(Unit)
      delay(delayMillis)
    }
  }
}

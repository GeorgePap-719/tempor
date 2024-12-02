package tempor

import kotlin.reflect.KClass
import kotlin.reflect.safeCast
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
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
  private val temporJob: Job

  init {
    temporJob = initTemporJob()
  }

  // Keep it simple.
  private fun initTemporJob(): Job = CoroutineScope(dispatcher).launch {
    while (true) {
      val slot = store.receive()
      val consumers = handlers[slot.type] ?: error("there is no registered handler for:${slot::class}")
      for (consumer in consumers) {
        val cast = slot.type.safeCast(slot.value)!!
//        val cast = slot.cast()
        consumer.handle(cast)
      }
    }
  }

  fun start() {
    runBlocking {
      temporJob.start()
      temporJob.join()
    }
  }

  fun <T : Any> tryAdd(value: T, type: KClass<T>) {
    store.trySend(value)
  }

  private class EventSlot<T : Any>(val value: T, val type: KClass<T>)
}


//@OptIn(ExperimentalCoroutinesApi::class)
//fun CoroutineScope.fixedPeriodTicker(
//  delayMillis: Long,
//): ReceiveChannel<Unit> {
//  return produce(capacity = 0) {
//    // We don't really need initial delay.
//    // delay(delayMillis)
//    while (true) {
//      channel.send(Unit)
//      delay(delayMillis)
//    }
//  }
//}

fun interface EventHandler<T> {
  fun handle(value: T)
}

fun main() {
  val engine = Tempor(handlers = handlers)
  engine.tryAdd("Hello world")
  engine.start()
}
package tempor

import kotlin.time.Duration
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

// High level design:
// 1 - simple API: for adding events, registering eventHandlers
// 2 - use channels under the hood to process events
// 3 - let's set aside for now the builder around this.
class TemporImpl<T>(
  private val handlers: List<EventHandler<T>>,
  private val dispatcher: CoroutineDispatcher
) : Tempor<T> {
  private val store = Channel<T>(100_000)

  // Track size so we can batch.
  private val size = atomic(0)

  private suspend fun emitImpl(event: T) {
    store.send(event)
    size += 1
  }

  private fun collectAll(): List<T> = buildList {
    val size = this@TemporImpl.size.value // maybe we can put a limit here
    for (i in 0..<size) {
      val event = store.tryReceive().getOrNull() ?: break
      add(event)
    }
    this@TemporImpl.size += -size
  }

  private val monitor = Channel<Unit>(0, BufferOverflow.DROP_LATEST)
  private val monitorJob: Job

  init {
    monitorJob = initMonitor()
  }

  private fun initMonitor(): Job = CoroutineScope(dispatcher).launch {
    monitor.consumeAsFlow().collect {
      println("-- COLLECTING -- ")
      val events = collectAll()
      if (handlers.size == 1) {
        val handler = handlers.first()
        handler.handle(events)
        return@collect
      }
      for (handler in handlers) {
        launch { handler.handle(events) }
      }
    }
  }

  override fun cancelAndJoin(duration: Duration): Unit = runBlocking {
    delay(duration)
    monitorJob.cancelAndJoin()
    store.cancel()
  }

  override suspend fun emit(event: T) {
    emitImpl(event)
    monitor.trySend(Unit)
  }
}
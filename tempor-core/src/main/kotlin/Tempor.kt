package tempor

import kotlin.time.Duration
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

interface Tempor<T> {
  suspend fun emit(event: T)

  fun cancelAndJoin(duration: Duration)
}

fun interface EventHandler<T> {
  fun handle(value: List<T>)
}

fun <T> Tempor(
  handlers: List<EventHandler<T>>,
  dispatcher: CoroutineDispatcher? = null
): Tempor<T> {
  val _dispatcher = dispatcher
    ?: Dispatchers.Default.limitedParallelism(1, "temporDispatcher")
  return TemporImpl(handlers, _dispatcher)
}
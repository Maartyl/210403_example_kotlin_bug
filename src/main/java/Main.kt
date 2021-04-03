package maartyl.example.bug

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.flow
import kotlin.coroutines.CoroutineContext

fun bad() {
  val ret = flow {

    // This variant does not work.
    //  "Suspension functions can be called only within coroutine body" at `adjustingCount`
    // This used to work in 1.4.21
    // Show this incorrect error in 1.4.30  (and always shown by updated IntelliJ Kotlin Plugin)

    val lb = currentCoroutineContext()[XElem]
      ?.let { { adjustingCount(it) } }
      ?: singleAdjusterForAlwaysFalse

    emit(5)
  }
}

fun ok() {
  // This variant works

  val lb = src()
    ?.let { { adjustingCount(it) } }
    ?: singleAdjusterForAlwaysFalse
}

fun ok2() {
  val ret = flow {
    // This variant works

    val lb = when (val it = currentCoroutineContext()[XElem]) {
      null -> singleAdjusterForAlwaysFalse
      else -> {
        { adjustingCount(it) }
      }
    }

    emit(5)
  }
}

fun main() {
  bad()
  ok()
  ok2()
}


// ---

private suspend fun adjustingCount(cp: XElem) {
  //...
}

interface XElem : CoroutineContext.Element {
  override val key: CoroutineContext.Key<*> get() = Key

  companion object Key : CoroutineContext.Key<XElem>
}

fun src(): XElem? {
  throw NotImplementedError()
}

private typealias LBlock = suspend CoroutineScope.() -> Unit

val singleAdjusterForAlwaysFalse: LBlock = {
  //...
}
package maartyl.example.bug

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.flow
import kotlin.coroutines.CoroutineContext

fun src(): XElem? {
  throw NotImplementedError()
}

fun ok() {
  // Works OK

  val lbb = src()
    ?.let { { adjustingCount(it) } }
    ?: singleAdjusterForAlwaysFalse
}

fun bad() {
  val ret = flow {

    // This variant does not work.
    //  "Suspension functions can be called only within coroutine body" at `adjustingCount`
    // This used to work in 1.4.21
    // Show this incorrect error in 1.4.30  (and always shown by updated IntelliJ Kotlin Plugin)

    val lb = currentCoroutineContext()[XElem]
      //?.takeUnless { it === defaultXElem } //irrelevant
      ?.let { { adjustingCount(it) } }
      ?: singleAdjusterForAlwaysFalse

    emit(5)
  }
}

fun againOK() {
  val ret = flow {
    // This variant works

    val lb = when (val it = currentCoroutineContext()[XElem]) {
      null, defaultXElem -> singleAdjusterForAlwaysFalse
      else -> {
        { adjustingCount(it) }
      }
    }

    emit(5)
  }
}

fun main() {
  bad()
}


// ---

interface XElem : CoroutineContext.Element {
  override val key: CoroutineContext.Key<*> get() = Key

  companion object Key : CoroutineContext.Key<XElem>
}

private val defaultXElem = object : XElem {}

private typealias LBlock = suspend CoroutineScope.() -> Unit

val singleAdjusterForAlwaysFalse: LBlock = {
  //...
}

private suspend fun adjustingCount(cp: XElem) {
  //...
}
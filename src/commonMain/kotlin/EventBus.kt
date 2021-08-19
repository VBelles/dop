import com.soywiz.korge.bus.GlobalBus
import com.soywiz.korio.async.launchImmediately
import com.soywiz.korio.lang.Closeable
import kotlinx.coroutines.CoroutineScope
import kotlin.reflect.KClass

// https://github.com/TobseF/Candy-Crush-Clone/blob/master/src/commonMain/kotlin/j4k/candycrush/lib/EventBus.kt

/**
 * Global event bus which distributes events to registered receivers.
 */
class EventBus(private val scope: CoroutineScope) {

    private val globalBus = GlobalBus()

    fun send(message: Any) {
        scope.launchImmediately {
            globalBus.send(message)
        }
    }

    fun <T : Any> register(clazz: KClass<out T>, handler: suspend (T) -> Unit): Closeable {
        return globalBus.register(clazz, handler)
    }

    inline fun <reified T : Any> register(noinline handler: suspend (T) -> Unit): Closeable {
        return register(T::class, handler)
    }
}
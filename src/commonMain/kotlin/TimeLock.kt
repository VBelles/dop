import com.soywiz.klock.DateTime

class TimeLock(private val delay: Double) {

    private var lastTick: Double = 0.0

    fun check(): Boolean {
        val now = DateTime.nowUnix()
        if (now - lastTick >= delay) {
            lastTick = now
            return true
        }
        return false
    }

    fun isReady(): Boolean {
        return DateTime.nowUnix() - lastTick >= delay
    }
}
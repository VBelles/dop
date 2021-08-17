import com.soywiz.korge.Korge
import com.soywiz.korge.view.anchor
import com.soywiz.korge.view.name
import com.soywiz.korge.view.position
import com.soywiz.korge.view.solidRect
import com.soywiz.korim.color.Colors
import com.soywiz.korma.geom.Point
import kotlinx.coroutines.delay
import kotlin.random.Random


suspend fun main() = Korge(width = 1920 / 2, height = 1080 / 2, bgcolor = Colors["#2b2b2b"]) {
    val bus = EventBus(this)
    solidRect(250, 250, Colors.GREEN)
        .name("parcel")
        .anchor(.5, .5)
        .position(this@Korge.width / 2, this@Korge.height / 2)

    player(bus)

    while (true) {
        enemy(bus, randomAtEdge(width, height))
        delay(2000)
    }
}

private fun randomAtEdge(width: Double, height: Double): Point {
    var p = Random.nextDouble(0.0, width + width + height + height)
    val pos = Point()
    if (p < (width + height)) {
        if (p < width) {
            pos.x = p
            pos.y = 0.0
        } else {
            pos.x = width
            pos.y = p - width
        }
    } else {
        p -= (width + height)
        if (p < width) {
            pos.x = width - p
            pos.y = height
        } else {
            pos.x = 0.0
            pos.y = height - (p - width)
        }
    }
    return pos
}
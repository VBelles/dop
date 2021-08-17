import com.soywiz.korge.view.*
import com.soywiz.korim.color.Colors
import com.soywiz.korma.geom.Point


fun Stage.enemy(bus: EventBus, startPosition: Point) {
    val speed = 50f
    val target = findViewByName("parcel")!!

    var parcelReached = false
    var hp = 3
    circle(30.0, Colors.RED) {
        addProp("enemy", true)
        position(startPosition)
        anchor(.5, .5)
        addUpdater { delta ->
            if (!parcelReached) {
                move(target.pos, speed, delta.seconds)
                parcelReached = collidesWith(target)
            }
        }
        bus.register<BulletHitEvent> { event ->
            if (event.target === this) {
                hp--
                if (hp <= 0) {
                    removeFromParent()
                }
            }
        }
    }

}


private fun View.move(target: Point, speed: Float, delta: Double) {
    val dir = target - pos
    dir.normalize()
    pos = pos + dir * (speed * delta)
}



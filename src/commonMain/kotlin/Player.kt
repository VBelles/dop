import com.soywiz.klock.TimeProvider
import com.soywiz.korev.Key
import com.soywiz.korev.MouseButton
import com.soywiz.korge.input.Input
import com.soywiz.korge.view.*
import com.soywiz.korma.geom.Point

fun Stage.player(bus: EventBus) {
    val speed = 150f
    val dir = Point()
    var lastShot = 0.0
    circle(30.0)
        .name("player")
        .anchor(.5, .5)
        .position(width / 2, height / 2)
        .addUpdater { delta ->
            move(input, dir, speed, delta.seconds)
            val now = TimeProvider.now().unixMillis
            if (input.mouseButtonPressed(MouseButton.LEFT) && now - lastShot >= 500) {
                stage?.bullet(bus, pos, input.mouse)
                lastShot = now
            }
        }

}


private fun View.move(input: Input, dir: Point, speed: Float, delta: Double) {
    dir.setToZero()
    if (input.keys.pressing(Key.W)) {
        dir.y -= 1
    }
    if (input.keys.pressing(Key.A)) {
        dir.x -= 1
    }
    if (input.keys.pressing(Key.S)) {
        dir.y += 1
    }
    if (input.keys.pressing(Key.D)) {
        dir.x += 1
    }
    if (dir.x != 0.0 && dir.y != 0.0) {
        dir.normalize()
    }
    pos = pos + dir * (speed * delta)
}

private fun Stage.bullet(bus: EventBus, position: Point, targetPosition: Point) {
    val dir = targetPosition - position
    dir.normalize()
    circle(10.0) {
        addProp("bullet", true)
        position(position)
        addUpdater {
            pos = pos + dir * 10f
        }
        onCollision({ view -> view.props["enemy"] == true }) { target ->
            bus.send(BulletHitEvent(target))
            removeFromParent()
        }
    }
}



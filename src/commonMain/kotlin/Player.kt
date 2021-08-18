import com.soywiz.klock.DateTime
import com.soywiz.klock.TimeSpan
import com.soywiz.korev.Key
import com.soywiz.korev.MouseButton
import com.soywiz.korge.input.Input
import com.soywiz.korge.view.*
import com.soywiz.korim.atlas.Atlas
import com.soywiz.korim.bitmap.Bitmap
import com.soywiz.korma.geom.Angle
import com.soywiz.korma.geom.Point
import com.soywiz.korma.geom.plus

fun Stage.player(bus: EventBus, spawn: Point, weapon: Bitmap, atlas: Atlas) {
    val speed = 150f
    val dir = Point()
    var lastShot = 0.0

    val runAnimation = atlas.getSpriteAnimation(prefix = "run", TimeSpan(120.0))
    sprite(runAnimation) {
        scale(-0.3, 0.3)
        name("player")
        anchor(.5, .5)
        position(spawn)
        addUpdater { delta ->
            move(input, dir, speed, delta.seconds)
            val now = DateTime.now().unixMillis
            if (input.mouseButtonPressed(MouseButton.LEFT) && now - lastShot >= 500) {
                stage?.bullet(bus, pos, mouseXY, weapon)
                lastShot = now
            }
        }
        playAnimationLooped()
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

private fun Stage.bullet(bus: EventBus, position: Point, targetPosition: Point, weapon: Bitmap) {
    val dir = targetPosition - position
    dir.normalize()
    sprite(weapon) {
        addProp("bullet", true)
        position(position)
        anchor(.5, .5)
        addUpdater { delta ->
            pos = pos + dir * 10f
            rotation += Angle(10.0 * delta.seconds)
        }
        onCollision({ view -> view.props["enemy"] == true }) { target ->
            bus.send(BulletHitEvent(target))
            removeFromParent()
        }
    }
}



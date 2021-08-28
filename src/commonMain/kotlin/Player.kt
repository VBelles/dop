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
import events.BulletHitEvent
import events.EnemyAttackEvent
import events.EventBus

fun Stage.player(bus: EventBus, spawn: Point, weapon: Bitmap, atlas: Atlas) {
    val speed = 150f
    val dir = Point()
    val timeLock = TimeLock(500.0)
    var hp = 500
    //val runAnimation = atlas.getSpriteAnimation(prefix = "run", TimeSpan(120.0))
    val attackAnimation = atlas.getSpriteAnimation(prefix = "attack", TimeSpan(120.0))

    val hpIndicator = text("HP: $hp", textSize = 20.0) {
        position(this@player.width - 150, this@player.height - 250)
    }

    sprite(attackAnimation) {
        scale(-0.3, 0.3)
        name("player")
        anchor(.5, .5)
        position(spawn)
        bus.register<EnemyAttackEvent> { attack ->
            hp -= attack.damage
            println(hp)
            hpIndicator.text = "HP: $hp"
            if (hp <= 0) {
                hpIndicator.text = "Parcel lost"
            }
        }
        addUpdater { delta ->
            move(input, dir, speed, delta.seconds)
            if (input.mouseButtonPressed(MouseButton.LEFT) && timeLock.check()) {
                playAnimation(attackAnimation)
                stage?.bullet(bus, pos, mouseXY, weapon)
            }
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



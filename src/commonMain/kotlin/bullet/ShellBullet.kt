package bullet

import Weapon
import com.soywiz.korge.view.*
import com.soywiz.korma.geom.Angle
import com.soywiz.korma.geom.Point
import com.soywiz.korma.geom.plus
import events.BulletHitEvent
import events.EventBus

fun Container.shellBullet(bus: EventBus, position: Point, targetPosition: Point, weapon: Weapon) {
    val dir = targetPosition - position
    dir.normalize()
    sprite(weapon.bitmap) {
        addProp("bullet", true)
        position(position)
        scaledHeight = 10.0
        scaledWidth = width * scaledHeight / height
        anchor(.5, .5)
        addUpdater { delta ->
            pos = pos + dir * 500 * delta.seconds
            rotation += Angle(10.0 * delta.seconds)
        }
        onCollision({ view -> view.props["enemy"] == true }) { target ->
            bus.send(BulletHitEvent(target, weapon.damage))
            removeFromParent()
        }
    }
}
package bullet

import Assets
import Weapon
import com.soywiz.klock.TimeSpan
import com.soywiz.korge.view.*
import com.soywiz.korma.geom.Angle
import com.soywiz.korma.geom.Point
import com.soywiz.korma.geom.plus
import events.BulletHitEvent
import events.EventBus

fun Container.shellBullet(bus: EventBus, position: Point, targetPosition: Point, weapon: Weapon, assets: Assets) {
    val dir = targetPosition - position
    dir.normalize()
    sprite(assets.getWeaponBitmap(weapon)) {
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
        addFixedUpdater(TimeSpan(5000.0), false) {
            removeFromParent()
        }
    }
}
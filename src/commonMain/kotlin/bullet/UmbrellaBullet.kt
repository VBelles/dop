package bullet

import Assets
import Weapon
import com.soywiz.klock.TimeSpan
import com.soywiz.korge.view.*
import com.soywiz.korma.geom.Angle
import com.soywiz.korma.geom.Point
import events.BulletHitEvent
import events.EventBus
import kotlin.math.PI
import kotlin.math.atan2

fun Container.umbrellaBullet(bus: EventBus, position: Point, targetPosition: Point, weapon: Weapon, assets: Assets) {
    var hp = 3
    val dir = targetPosition - position
    dir.normalize()
    val collided = mutableSetOf<View>()
    val angle = atan2(dir.y, dir.x) + PI / 2.0
    sprite(assets.getWeaponBitmap(weapon)) {
        rotation(Angle(angle))
        addProp("bullet", true)
        position(position)
        scaledHeight = 30.0
        scaledWidth = width * scaledHeight / height
        anchor(.5, .5)
        addUpdater { delta ->
            pos = pos + dir * 400 * delta.seconds
        }
        onCollision({ view -> view.props["enemy"] == true && !collided.contains(view) }) { target ->
            bus.send(BulletHitEvent(target, weapon.damage))
            collided.add(target)
            if (--hp <= 0) {
                removeFromParent()
            }
        }
        addFixedUpdater(TimeSpan(5000.0), false) {
            removeFromParent()
        }
    }
}
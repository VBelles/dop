package bullet

import Assets
import Weapon
import com.soywiz.klock.TimeSpan
import com.soywiz.korge.view.*
import com.soywiz.korio.async.launch
import com.soywiz.korma.geom.Angle
import com.soywiz.korma.geom.Point
import com.soywiz.korma.geom.plus
import events.BulletHitEvent
import events.EventBus
import lerp

fun Container.ballBullet(bus: EventBus, startPos: Point, targetPosition: Point, weapon: Weapon, assets: Assets) {
    val explosion = assets.explosionAtlas.getSpriteAnimation(prefix = "explosionSmoke", TimeSpan(120.0))
    val dir = targetPosition - startPos
    dir.normalize()
    sprite(assets.ballBitmap) {
        position(Point(startPos))
        scaledHeight = 10.0
        scaledWidth = width * scaledHeight / height
        anchor(.5, .5)
        addUpdaterWithViews { views, delta ->
            // pos = pos + dir * 10f * delta.seconds
            rotation += Angle(5.0 * delta.seconds)

            val arcHeight = 50.0
            val x0 = startPos.x
            val x1 = targetPosition.x
            val dist = x1 - x0
            val nextX = pos.x + dir.x * 350f * delta.seconds
            val baseY = lerp(startPos.y, targetPosition.y, (nextX - x0) / dist)
            val arc = arcHeight * (nextX - x0) * (nextX - x1) / (-0.25f * dist * dist)
            pos = Point(nextX, baseY - arc)

            if (pos.distanceTo(targetPosition) < 10f) {
                val explosionSprite = sprite(explosion) {
                    smoothing = false
                    position(targetPosition)
                    center()
                    playAnimation()
                    addFixedUpdater(TimeSpan(explosion.size * 120.0), false) {
                        removeFromParent()
                    }
                }
                views.launch { assets.explosionSound.play() }
                root.foreachDescendant { target ->
                    if (target.props["enemy"] == true && target.collidesWith(explosionSprite)) {
                        bus.send(BulletHitEvent(target, weapon.damage))
                    }
                }
                removeFromParent()
            }
        }
        addFixedUpdater(TimeSpan(5000.0), false) {
            removeFromParent()
        }
    }
}

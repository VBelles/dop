package bullet

import Weapon
import com.soywiz.klock.TimeSpan
import com.soywiz.korge.view.*
import com.soywiz.korim.atlas.readAtlas
import com.soywiz.korio.file.std.resourcesVfs
import com.soywiz.korma.geom.Angle
import com.soywiz.korma.geom.Point
import com.soywiz.korma.geom.plus
import events.BulletHitEvent
import events.EventBus
import lerp

suspend fun Container.ballBullet(bus: EventBus, startPos: Point, targetPosition: Point, weapon: Weapon) {
    val atlas = resourcesVfs["sprites/explosion.xml"].readAtlas()
    val explosion = atlas.getSpriteAnimation(prefix = "explosionSmoke", TimeSpan(120.0))
    val dir = targetPosition - startPos
    dir.normalize()
    sprite(weapon.bitmap) {
        position(Point(startPos))
        scaledHeight = 10.0
        scaledWidth = width * scaledHeight / height
        anchor(.5, .5)
        addUpdater { delta ->
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
                root.foreachDescendant { target ->
                    if (target.props["enemy"] == true && target.collidesWith(explosionSprite)) {
                        bus.send(BulletHitEvent(target, weapon.damage))
                    }
                }
                removeFromParent()
            }
        }
    }
}

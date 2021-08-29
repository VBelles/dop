package bullet

import Weapon
import com.soywiz.klock.TimeSpan
import com.soywiz.korge.view.*
import com.soywiz.korim.atlas.readAtlas
import com.soywiz.korim.bitmap.Bitmap
import com.soywiz.korio.file.std.resourcesVfs
import com.soywiz.korma.geom.Angle
import com.soywiz.korma.geom.Point
import com.soywiz.korma.geom.plus
import events.BulletHitEvent
import events.EnemyAttackEvent
import events.EventBus
import lerp

suspend fun Container.enemyBullet(
    bus: EventBus,
    startPos: Point,
    targetPosition: Point,
    weapon: Weapon,
) {
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
                sprite(explosion) {
                    smoothing = false
                    position(targetPosition)
                    center()
                    playAnimation()
                    addFixedUpdater(TimeSpan(explosion.size * 120.0), false) {
                        removeFromParent()
                    }
                }
                bus.send(EnemyAttackEvent(weapon.damage))
                removeFromParent()
            }
        }
        addFixedUpdater(TimeSpan(5000.0), false) {
            removeFromParent()
        }
    }
}

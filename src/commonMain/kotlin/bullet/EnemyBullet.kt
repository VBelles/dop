package bullet

import Assets
import Weapon
import com.soywiz.klock.TimeSpan
import com.soywiz.korge.view.*
import com.soywiz.korio.async.launch
import com.soywiz.korma.geom.Angle
import com.soywiz.korma.geom.Point
import com.soywiz.korma.geom.plus
import events.EnemyAttackEvent
import events.EventBus
import lerp
import playFixed
import kotlin.math.abs
import kotlin.random.Random

fun Container.enemyBullet(
    bus: EventBus,
    startPos: Point,
    targetPosition: Point,
    weapon: Weapon,
    assets: Assets
) {
    val explosion = assets.melonAtlas.getSpriteAnimation(prefix = "exploding", TimeSpan(120.0))
    val dir = targetPosition - startPos
    targetPosition.x += Random.nextInt(0, 150)
    dir.normalize()
    sprite(assets.getWeaponBitmap(weapon)) {
        position(Point(startPos))
        anchor(.5, .5)
        addUpdaterWithViews { views, delta ->
            rotation += Angle(5.0 * delta.seconds)

            val arcHeight = 50.0
            val x0 = startPos.x
            val x1 = targetPosition.x
            val dist = x1 - x0
            val nextX = pos.x + dir.x * 350f * delta.seconds
            val baseY = lerp(startPos.y, targetPosition.y, (nextX - x0) / dist)
            val arc = arcHeight * (nextX - x0) * (nextX - x1) / (-0.25f * dist * dist)
            pos = Point(nextX, baseY - arc)

            if (abs(pos.x - targetPosition.x) < 10) {
                sprite(explosion) {
                    smoothing = false
                    position(targetPosition)
                    center()
                    views.launch { assets.explosionSound.playFixed() }
                    playAnimation()
                    addFixedUpdater(TimeSpan(explosion.size * 120.0), false) {
                        removeFromParent()
                    }
                }
                bus.send(EnemyAttackEvent(1))
                removeFromParent()
            }
        }
        addFixedUpdater(TimeSpan(5000.0), false) {
            removeFromParent()
        }
    }
}

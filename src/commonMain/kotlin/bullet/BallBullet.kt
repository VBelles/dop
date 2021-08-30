package bullet

import Assets
import Weapon
import com.soywiz.klock.TimeSpan
import com.soywiz.korge.view.*
import com.soywiz.korge.view.tween.scaleTo
import com.soywiz.korio.async.launch
import com.soywiz.korma.geom.Angle
import com.soywiz.korma.geom.Point
import com.soywiz.korma.geom.plus
import events.BulletHitEvent
import events.EventBus
import lerp
import playFixed
import kotlin.math.abs

fun Container.ballBullet(bus: EventBus, startPos: Point, targetPosition: Point, weapon: Weapon, assets: Assets) {
    val explosion = assets.melonAtlas.getSpriteAnimation(prefix = "exploding", TimeSpan(120.0))
    val dir = targetPosition - startPos
    dir.normalize()
    sprite(assets.melonBitmap) {
        position(Point(startPos))
        anchor(.5, .5)
        addUpdaterWithViews { views, delta ->
            // pos = pos + dir * 10f * delta.seconds
            rotation += Angle(5.0 * delta.seconds)

            val arcHeight = 50.0
            val x0 = startPos.x
            val x1 = targetPosition.x
            val dist = x1 - x0
            val nextX = pos.x + dir.x * 350 * delta.seconds
            val baseY = lerp(startPos.y, targetPosition.y, (nextX - x0) / dist)
            val arc = arcHeight * (nextX - x0) * (nextX - x1) / (-0.25 * dist * dist)
            pos = Point(nextX, baseY - arc)
            if (abs(pos.x - targetPosition.x) < 10) {
                pos = targetPosition
                sprite(explosion) {
                    views.launch {
                        scaleTo(2.5, 2.5, TimeSpan(700.0))
                    }
                    smoothing = false
                    position(targetPosition)
                    center()
                    playAnimation()
                    addFixedUpdater(TimeSpan(explosion.size * 120.0), false) {
                        removeFromParent()
                    }
                }
                views.launch { assets.explosionSound.playFixed() }
                root.foreachDescendant { target ->
                    if (target.props["enemy"] == true && target.pos.distanceTo(pos) < 60) {
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

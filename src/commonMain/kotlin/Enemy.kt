import bullet.enemyBullet
import com.soywiz.klock.TimeSpan
import com.soywiz.korge.view.*
import com.soywiz.korim.atlas.Atlas
import com.soywiz.korio.async.launch
import com.soywiz.korma.geom.Point
import events.BulletHitEvent
import events.EnemyAttackEvent
import events.EventBus
import kotlin.random.Random


enum class EnemyType {
    Melee, Range
}

private enum class Action {
    Running, Attacking, Dying
}

suspend fun Stage.enemy(
    bus: EventBus,
    spawnX: Double,
    spawnMinY: Double,
    spawnMaxY: Double,
    baseX: Double,
    atlas: Atlas,
    type: EnemyType,
    weapon: Weapon,
) {
    val speed = 50f
    var hp = 2
    val runAnimation = atlas.getSpriteAnimation(prefix = "walk", TimeSpan(120.0))
    val attackAnimation = atlas.getSpriteAnimation(prefix = "attack", TimeSpan(120.0))
    var action = Action.Running

    val fireRate = if (type == EnemyType.Melee) 1500.0 else 2500.0
    val timeLock = TimeLock(fireRate)
    val range = if (type == EnemyType.Melee) 0.0 else Random.nextDouble(280.0, 320.0)

    sprite(runAnimation) {
        addProp("enemy", true)
        scale = 0.3
        position(spawnX, Random.nextDouble(spawnMaxY, spawnMinY) - scaledHeight)
        playAnimationLooped()

        val eventListener = bus.register<BulletHitEvent> { event ->
            if (event.target === this) {
                hp--
                if (hp <= 0) {
                    addProp("enemy", false)
                    scaleX = -scaleX
                    x += scaledHeight
                    playAnimationLooped(runAnimation)
                    action = Action.Dying
                }
            }
        }

        addUpdaterWithViews { views, delta ->
            when (action) {
                Action.Running -> {
                    x += delta.seconds * speed
                    if (x + range + scaledWidth >= baseX) {
                        action = Action.Attacking
                    }
                }
                Action.Attacking -> {
                    when (type) {
                        EnemyType.Melee -> if (timeLock.check()) {
                            playAnimation(attackAnimation)
                            bus.send(EnemyAttackEvent(5.0))
                        }
                        EnemyType.Range -> if (timeLock.check()) {
                            playAnimation(attackAnimation)
                            views.launch {
                                enemyBullet(bus, pos, Point(baseX, pos.y + scaledHeight / 2), weapon)
                            }
                        }
                    }
                }
                Action.Dying -> {
                    x -= delta.seconds * speed * 2
                    alpha -= 0.5 * delta.seconds
                    if (alpha <= 0) {
                        removeFromParent()
                        eventListener.close()
                    }
                }
            }
        }
    }
}



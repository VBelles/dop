import com.soywiz.klock.DateTime
import com.soywiz.klock.TimeSpan
import com.soywiz.korge.view.*
import com.soywiz.korim.atlas.Atlas
import events.BulletHitEvent
import events.EnemyAttackEvent
import events.EventBus
import kotlin.random.Random


private enum class Action {
    Running, Attacking, Dying
}

fun Stage.enemy(
    bus: EventBus,
    spawnX: Double,
    spawnMinY: Double,
    spawnMaxY: Double,
    baseX: Double,
    atlas: Atlas
) {
    val speed = 50f
    var hp = 2
    val runAnimation = atlas.getSpriteAnimation(prefix = "walk", TimeSpan(120.0))
    val attackAnimation = atlas.getSpriteAnimation(prefix = "attack", TimeSpan(120.0))
    var action = Action.Running
    var lastAttack = 0.0
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

        addUpdater { delta ->
            when (action) {
                Action.Running -> {
                    x += delta.seconds * speed
                    if (x > baseX - scaledWidth) {
                        action = Action.Attacking
                        playAnimationLooped(attackAnimation)
                    }
                }
                Action.Attacking -> {
                    val now = DateTime.nowUnix()
                    if (now - lastAttack >= 1000) {
                        lastAttack = now
                        bus.send(EnemyAttackEvent(5))
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



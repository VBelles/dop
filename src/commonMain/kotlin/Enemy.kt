import bullet.enemyBullet
import com.soywiz.klock.TimeSpan
import com.soywiz.korge.view.*
import com.soywiz.korinject.injector
import com.soywiz.korio.async.launch
import com.soywiz.korma.geom.Point
import events.*
import kotlin.random.Random


enum class EnemyType {
    Melee, Range
}

private enum class Action {
    Running, Attacking, Dying
}

suspend fun Container.enemy(
    startPosition: Point,
    baseX: Double,
    type: EnemyType,
    weapon: Weapon,
) {
    val bus = injector().get<EventBus>()
    val speed = 50f
    var hp = 2
    val assets = injector().get<Assets>()
    val runAnimation = assets.invaderAtlas.getSpriteAnimation(prefix = "walk", TimeSpan(120.0))
    val attackAnimation = assets.invaderAtlas.getSpriteAnimation(prefix = "attack", TimeSpan(120.0))
    var action = Action.Running

    val fireRate = if (type == EnemyType.Melee) 1500.0 else 2500.0
    val timeLock = TimeLock(fireRate)
    val range = if (type == EnemyType.Melee) 0.0 else Random.nextDouble(280.0, 320.0)
    val weaponOffset = Point(10.0, 10.0)
    var idleWeapon: View? = null

    val walkTimeLock = TimeLock(750.0)
    var steps = 0

    val invader = sprite(runAnimation) {
        addProp("enemy", true)
        scale = 0.3
        center()
        position(startPosition)
        playAnimationLooped()

        fun die() {
            hp = 0
            addProp("enemy", false)
            scaleX = -scaleX
            idleWeapon?.removeFromParent()
            playAnimationLooped(runAnimation)
            action = Action.Dying
        }

        val eventListener = bus.register<BulletHitEvent> { event ->
            if (event.target === this) {
                assets.hitSound.play()
                if (--hp <= 0) {
                    bus.send(EnemyDiedEvent)
                    die()
                }
            }
        }

        val waveClearListener = bus.register<ClearWaveEvent> {
            die()
        }

        addUpdaterWithViews { views, delta ->
            when (action) {
                Action.Running -> {
                    x += delta.seconds * speed
                    if (x + range + scaledWidth >= baseX) {
                        action = Action.Attacking
                    }
                    if (walkTimeLock.check()) {
                        views.launch {
                            if (steps % 2 == 0) assets.step1Sound.play() else assets.step2Sound.play()
                        }
                        steps++
                    }
                }
                Action.Attacking -> {
                    when (type) {
                        EnemyType.Melee -> if (timeLock.check()) {
                            views.launch { assets.throwSound.play() }
                            playAnimation(attackAnimation)
                            bus.send(EnemyAttackEvent(5.0))
                        }
                        EnemyType.Range -> if (timeLock.check()) {
                            views.launch { assets.throwSound.play() }
                            playAnimation(attackAnimation)
                            enemyBullet(bus, pos + weaponOffset, Point(baseX, pos.y), weapon, assets)
                        }
                    }
                }
                Action.Dying -> {
                    x -= delta.seconds * speed * 2
                    alpha -= 0.5 * delta.seconds
                    if (alpha <= 0) {
                        removeFromParent()
                        eventListener.close()
                        waveClearListener.close()
                    }
                }
            }
        }
    }

    if (type == EnemyType.Range) {
        idleWeapon = sprite(assets.getWeaponBitmap(weapon)) {
            scaledHeight = 10.0
            scaledWidth = width * scaledHeight / height
            center()
            addUpdater {
                visible = timeLock.isReady()
                pos = invader.pos + weaponOffset
            }
        }
    }
}



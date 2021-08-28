import com.soywiz.klock.TimeSpan
import com.soywiz.korev.Key
import com.soywiz.korev.MouseButton
import com.soywiz.korge.input.Input
import com.soywiz.korge.view.*
import com.soywiz.korim.atlas.Atlas
import com.soywiz.korim.atlas.readAtlas
import com.soywiz.korim.format.readBitmap
import com.soywiz.korio.async.launch
import com.soywiz.korio.file.std.resourcesVfs
import com.soywiz.korma.geom.Angle
import com.soywiz.korma.geom.Point
import com.soywiz.korma.geom.plus
import events.BulletHitEvent
import events.EnemyAttackEvent
import events.EventBus


private suspend fun initInventory() = Inventory(
    weapons = listOf(
        Weapon(
            price = 0,
            bought = true,
            name = "Shell",
            description = "Hard, fast and precise shells that hits up to 1 invader\ndealing 5 damage\nCan shoot every 0,5s",
            damage = 5.0,
            fireRate = 500.0,
            bitmap = resourcesVfs["sprites/shell.png"].readBitmap(),
            type = Weapon.Type.Shell,
        ),
        Weapon(
            price = 7500,
            bought = false,
            name = "Umbrella",
            description = "Like a javelin that go through 3 invaders dealing 5 damage\nto each\nCan shoot every 1s",
            damage = 5.0,
            fireRate = 1000.0,
            bitmap = resourcesVfs["sprites/beach_umbrella.png"].readBitmap(),
            type = Weapon.Type.Umbrella,
        ),
        Weapon(
            price = 21000,
            bought = false,
            name = "Ball",
            description = "A very inflated beach ball that explodes on contact with\nsand or invaders dealing 5 area damage\nCan shoot every 2s",
            damage = 5.0,
            fireRate = 500.0,
            bitmap = resourcesVfs["sprites/beach_ball.png"].readBitmap(),
            type = Weapon.Type.Ball,
        ),
    ),
    0, 0,
)

suspend fun Container.player(bus: EventBus, spawn: Point, atlas: Atlas) {
    val speed = 150f
    val dir = Point()
    val timeLock = TimeLock(500.0)
    var hp = 500
    var selectedWeapon: Weapon? = null
    var inventory = initInventory()
    //val runAnimation = atlas.getSpriteAnimation(prefix = "run", TimeSpan(120.0))
    val attackAnimation = atlas.getSpriteAnimation(prefix = "attack", TimeSpan(120.0))

    val hpIndicator = text("HP: $hp", textSize = 20.0) {
        position(this@player.width - 150, this@player.height - 250)
    }

    sprite(attackAnimation) {
        scale(-0.3, 0.3)
        name("player")
        anchor(.5, .5)
        position(spawn)
        bus.register<EnemyAttackEvent> { attack ->
            hp -= attack.damage
            println(hp)
            hpIndicator.text = "HP: $hp"
            if (hp <= 0) {
                hpIndicator.text = "Parcel lost"
            }
        }

        var weaponReady = false

        addUpdaterWithViews { views, delta ->
            val input = views.input
            move(input, dir, speed, delta.seconds)

            if (input.mouseButtonPressed(MouseButton.LEFT) && timeLock.check()) {
                playAnimation(attackAnimation)
                launch(views.coroutineContext) {
                    when (selectedWeapon?.type) {
                        Weapon.Type.Shell -> bullet(bus, Point(pos), Point(stage!!.mouseXY), selectedWeapon!!)
                        Weapon.Type.Umbrella -> ballBullet(bus, Point(pos), Point(stage!!.mouseXY), selectedWeapon!!)
                        Weapon.Type.Ball -> ballBullet(bus, Point(pos), Point(stage!!.mouseXY), selectedWeapon!!)
                        null -> Unit
                    }
                }
            }

            if (input.keys.justPressed(Key.N1)) {
                selectedWeapon = inventory.weapons[0]
                println("Changed weapon to $selectedWeapon")

            } else if (input.keys.justPressed(Key.N2)) {
                selectedWeapon = inventory.weapons[1]
                println("Changed weapon to $selectedWeapon")

            } else if (input.keys.justPressed(Key.N3)) {
                selectedWeapon = inventory.weapons[2]
                println("Changed weapon to $selectedWeapon")
            }
        }
    }
}


private fun View.move(input: Input, dir: Point, speed: Float, delta: Double) {
    dir.setToZero()
    if (input.keys.pressing(Key.W)) {
        dir.y -= 1
    }
    if (input.keys.pressing(Key.A)) {
        dir.x -= 1
    }
    if (input.keys.pressing(Key.S)) {
        dir.y += 1
    }
    if (input.keys.pressing(Key.D)) {
        dir.x += 1
    }
    if (dir.x != 0.0 && dir.y != 0.0) {
        dir.normalize()
    }
    pos = pos + dir * (speed * delta)
}

private fun Container.bullet(bus: EventBus, position: Point, targetPosition: Point, weapon: Weapon) {
    val dir = targetPosition - position
    dir.normalize()
    sprite(weapon.bitmap) {
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
    }
}

fun lerp(a: Double, b: Double, f: Double): Double {
    return a + f * (b - a)
}

private suspend fun Container.ballBullet(bus: EventBus, startPos: Point, targetPosition: Point, weapon: Weapon) {
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



import bullet.ballBullet
import bullet.shellBullet
import bullet.umbrellaBullet
import com.soywiz.klock.TimeSpan
import com.soywiz.korev.Key
import com.soywiz.korev.MouseButton
import com.soywiz.korge.input.Input
import com.soywiz.korge.view.*
import com.soywiz.korim.bitmap.slice
import com.soywiz.korinject.injector
import com.soywiz.korma.geom.Point
import events.EnemyAttackEvent
import events.EventBus


suspend fun Container.player(spawn: Point) {
    val bus = injector().get<EventBus>()
    val assets = injector().get<Assets>()
    val inventory = injector().get<Inventory>()


    val speed = 150f
    val dir = Point()
    val timeLock = TimeLock(500.0)
    var hp = 500

    var selectedWeapon: Weapon = inventory.weapons.first()
    //val runAnimation = atlas.getSpriteAnimation(prefix = "run", TimeSpan(120.0))
    val attackAnimation = assets.playerAtlas.getSpriteAnimation(prefix = "attack", TimeSpan(120.0))
    val weaponOffset = Point(-10.0, 6.0)

    val hpIndicator = text("HP: $hp", textSize = 20.0) {
        position(this@player.width - 150, this@player.height - 250)
    }


    addUpdaterWithViews { views, _ ->
        val input = views.input
        if (input.keys.justPressed(Key.N1)) {
            selectedWeapon = inventory.weapons[0]
        } else if (input.keys.justPressed(Key.N2)) {
            selectedWeapon = inventory.weapons[1]
        } else if (input.keys.justPressed(Key.N3)) {
            selectedWeapon = inventory.weapons[2]
        }
    }

    sprite(attackAnimation) {
        smoothing = false
        scale(-0.3, 0.3)
        name("player")
        anchor(.5, .5)
        position(spawn)
        bus.register<EnemyAttackEvent> { attack ->
            hp -= attack.damage.toInt()
            println(hp)
            hpIndicator.text = "HP: $hp"
            if (hp <= 0) {
                hpIndicator.text = "Parcel lost"
            }
        }

        val shop by lazy { root.findViewByName("shop")!! }
        addUpdaterWithViews { views, delta ->
            move(views.input, dir, speed, delta.seconds)
            if (views.input.mouseButtonPressed(MouseButton.LEFT) && !shop.visible && timeLock.check()) {
                playAnimation(attackAnimation)
                val startPosition = pos + weaponOffset
                val target = Point(stage!!.mouseXY)
                when (selectedWeapon.type) {
                    Weapon.Type.Shell -> shellBullet(bus, startPosition, target, selectedWeapon, assets)
                    Weapon.Type.Umbrella -> umbrellaBullet(bus, startPosition, target, selectedWeapon, assets)
                    Weapon.Type.Ball -> ballBullet(bus, startPosition, target, selectedWeapon, assets)
                }
            }
        }
    }


    // Weapon on hand while have ammo
    sprite(assets.getWeaponBitmap(selectedWeapon)) {
        fun onWeaponChanged() {
            bitmap = assets.getWeaponBitmap(selectedWeapon).slice()
            center()
            scaledHeight = 10.0
            scaledWidth = width * scaledHeight / height
        }

        var weapon = selectedWeapon
        onWeaponChanged()
        addUpdater {
            pos = root.findViewByName("player")!!.pos + weaponOffset
            visible = timeLock.isReady()
            if (weapon.type != selectedWeapon.type) {
                weapon = selectedWeapon
                onWeaponChanged()
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
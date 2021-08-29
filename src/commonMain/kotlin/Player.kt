import bullet.ballBullet
import bullet.shellBullet
import bullet.umbrellaBullet
import com.soywiz.klock.TimeSpan
import com.soywiz.korev.Key
import com.soywiz.korge.input.Input
import com.soywiz.korge.input.onClick
import com.soywiz.korge.view.*
import com.soywiz.korim.bitmap.slice
import com.soywiz.korim.color.Colors
import com.soywiz.korinject.injector
import com.soywiz.korma.geom.Point
import events.EnemyAttackEvent
import events.EventBus
import events.WeaponBoughtEvent


suspend fun Container.player(spawn: Point) {
    val bus = injector().get<EventBus>()
    val assets = injector().get<Assets>()
    val weapons = injector().get<List<Weapon>>()
    val inventory = injector().get<Inventory>()

    val shootLock = TimeLock(500.0)
    var hp = 500

    var selectedWeapon: Weapon = inventory.weapons.first()
    //val runAnimation = atlas.getSpriteAnimation(prefix = "run", TimeSpan(120.0))
    val attackAnimation = assets.playerAtlas.getSpriteAnimation(prefix = "attack", TimeSpan(120.0))
    val weaponOffset = Point(-10.0, 6.0)

    val hpIndicator = text("HP: $hp", textSize = 20.0) {
        position(this@player.width - 150, this@player.height - 250)
    }


    fun changeWeapon(weapon: Weapon) {
        if (weapon in inventory.weapons) {
            selectedWeapon = weapon
            weapons.forEachIndexed { index, w ->
                val view = root.findViewByName("weapon_slot_$index") as RoundRect
                view.strokeThickness = if (selectedWeapon.type == w.type) 3.0 else 0.0
            }
        }
    }

    val shop by lazy { root.findViewByName("shop")!! }
    val player by lazy { root.findViewByName("player") as Sprite }
    onClick {
        if (!shop.visible && shootLock.check()) {
            player.playAnimation(attackAnimation)
            val startPosition = player.pos + weaponOffset
            val target = Point(stage!!.mouseXY)
            when (selectedWeapon.type) {
                Weapon.Type.Shell -> shellBullet(bus, startPosition, target, selectedWeapon, assets)
                Weapon.Type.Umbrella -> umbrellaBullet(bus, startPosition, target, selectedWeapon, assets)
                Weapon.Type.Ball -> ballBullet(bus, startPosition, target, selectedWeapon, assets)
            }
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
            visible = shootLock.isReady()
            if (weapon.type != selectedWeapon.type) {
                weapon = selectedWeapon
                onWeaponChanged()
            }
        }
    }


    container {
        var offset = 0.0
        weapons.forEachIndexed { index, weapon ->
            container {
                x = offset
                offset += 60.0
                roundRect(
                    width = 50.0,
                    height = 50.0,
                    rx = 15.0,
                    ry = 15.0,
                    fill = Colors.BLACK.withA(70),
                ) {
                    name("weapon_slot_$index")
                    onClick { changeWeapon(weapon) }
                    strokeThickness = if (selectedWeapon.type == weapon.type) 3.0 else 0.0
                    sprite(assets.getWeaponBitmap(weapon)) {
                        smoothing = false
                        scaledHeight = 30.0
                        scaledWidth = width * 30.0 / height
                        centerOn(parent!!)
                    }
                }
                alpha = if (weapon !in inventory.weapons) 0.25 else 1.0

                bus.register<WeaponBoughtEvent> { event ->
                    if (event.weapon.type == weapon.type) {
                        alpha = 1.0
                    }
                }
            }
        }
        centerXOnStage()
        alignBottomToBottomOf(root, 10)

        addUpdaterWithViews { views, _ ->
            val input = views.input
            val index = when {
                input.keys.justPressed(Key.N1) -> 0
                input.keys.justPressed(Key.N2) -> 1
                input.keys.justPressed(Key.N3) -> 2
                else -> -1
            }
            inventory.weapons.getOrNull(index)?.let { changeWeapon(it) }
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
import com.soywiz.korge.input.onClick
import com.soywiz.korge.view.*
import com.soywiz.korim.color.Colors
import com.soywiz.korinject.injector
import events.EnemyDiedEvent
import events.EventBus
import events.NextWaveEvent
import events.WeaponBoughtEvent


suspend fun Container.shop() {

    val bus = injector().get<EventBus>()
    val weapons = injector().get<List<Weapon>>()
    val inventory = injector().get<Inventory>()
    val assets = injector().get<Assets>()

    var selectedIndex = 0

    fun updateSelected(index: Int) {
        val previousSelectedView = findViewByName("weapon_$selectedIndex") as RoundRect
        val view = findViewByName("weapon_$index") as RoundRect
        selectedIndex = index
        previousSelectedView.strokeThickness = 0.0
        view.strokeThickness = 5.0
        val weapon = weapons[index]
        (findViewByName("description") as Text).text = weapon.description

        val buyText = findViewByName("buyText") as Text
        buyText.text = when {
            weapon in inventory.weapons -> "Owned"
            weapon.price > inventory.money -> "Costs ${weapon.price}"
            else -> "Buy by ${weapon.price}"
        }
        buyText.centerOn(buyText.parent!!)
        (findViewByName("buyBackground") as RoundRect).fill = when {
            weapon in inventory.weapons || weapon.price > inventory.money -> Colors.BLACK.withA(70)
            else -> Colors["#43a047"]
        }
    }

    fun buyWeapon() {
        val weapon = weapons[selectedIndex]
        if (weapon in inventory.weapons || weapon.price > inventory.money) return
        inventory.money -= weapon.price

        inventory.weapons = inventory.weapons + weapon

        bus.send(WeaponBoughtEvent(weapon))

        updateSelected(selectedIndex)
        (findViewByName("money") as Text).text = "Money ${inventory.money}"
    }

    container {
        var offset = 0.0
        weapons.forEachIndexed { index, weapon ->
            container {
                x = offset
                offset += 120.0
                val image = roundRect(
                    width = 100.0,
                    height = 100.0,
                    rx = 15.0,
                    ry = 15.0,
                    fill = Colors.BLACK.withA(70),
                ) {
                    name("weapon_$index")
                    onClick { updateSelected(index) }
                    sprite(assets.getWeaponBitmap(weapon)) {
                        smoothing = false
                        scaledHeight = 50.0
                        scaledWidth = width * 50.0 / height
                        centerOn(parent!!)
                    }
                }
                text(weapon.name, color = Colors.BLACK) {
                    centerXOn(image)
                    alignTopToBottomOf(image, 5)
                }
            }
        }
        centerXOnStage()
        alignBottomToBottomOf(root, 200)
    }

    container {
        roundRect(
            width = 400.0,
            height = 80.0,
            rx = 15.0,
            ry = 15.0,
            fill = Colors.BLACK.withA(70),
        )
        text("", color = Colors.WHITE) {
            name("description")
            x = 10.0
            y = 10.0
        }
        alignBottomToBottomOf(root, 80)
        centerXOnStage()
    }

    container {
        roundRect(
            width = 100.0,
            height = 30.0,
            rx = 15.0,
            ry = 15.0,
            fill = Colors["#43a047"],
        ) {
            name("buyBackground")
        }
        text("", color = Colors.WHITE) {
            name("buyText")
        }
        alignBottomToBottomOf(root, 250)
        alignLeftToLeftOf(root, 60)
        onClick { buyWeapon() }
    }

    container {
        roundRect(
            width = 100.0,
            height = 30.0,
            rx = 15.0,
            ry = 15.0,
            fill = Colors["#43a047"].withA(180),
        ) {
            name("buyBackground")
        }
        alignBottomToBottomOf(root, 250)
        alignRightToRightOf(root, 60)
        onClick { bus.send(NextWaveEvent) }
        text("Next wave", color = Colors.WHITE) {
            centerOn(parent!!)
        }

    }

    container {
        text("Money ${inventory.money}") {
            name("money")
        }
        alignBottomToBottomOf(root, 10)
        alignLeftToLeftOf(root, 10)
    }


    bus.register<EnemyDiedEvent> {
        inventory.money += 50
        (findViewByName("money") as Text).text = "Money ${inventory.money}"
    }

    updateSelected(0)

}




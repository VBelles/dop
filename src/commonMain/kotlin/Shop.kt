import com.soywiz.korge.input.onClick
import com.soywiz.korge.view.*
import com.soywiz.korim.bitmap.Bitmap
import com.soywiz.korim.color.Colors
import com.soywiz.korim.format.readBitmap
import com.soywiz.korio.file.std.resourcesVfs
import events.EventBus


suspend fun Stage.shop(bus: EventBus, inventory: Inventory) {
    var money = 10000
    var selectedIndex = 0

    fun updateSelected(index: Int) {
        val previousSelectedView = findViewByName("weapon_$selectedIndex") as RoundRect
        val view = findViewByName("weapon_$index") as RoundRect
        selectedIndex = index
        previousSelectedView.strokeThickness = 0.0
        view.strokeThickness = 5.0
        val weapon = inventory.weapons[index]
        (findViewByName("description") as Text).text = weapon.description

        val buyText = findViewByName("buyText") as Text
        buyText.text = when {
            weapon.bought -> "Owned"
            weapon.price > money -> "Costs ${weapon.price}"
            else -> "Buy by ${weapon.price}"
        }
        buyText.centerOn(buyText.parent!!)
        (findViewByName("buyBackground") as RoundRect).fill = when {
            weapon.bought || weapon.price > money -> Colors.BLACK.withA(70)
            else -> Colors["#43a047"]
        }
    }

    fun buyWeapon() {
        val weapon = inventory.weapons[selectedIndex]
        if (weapon.bought || weapon.price > money) return
        money -= weapon.price

        /*shopState = inventory.copy(weapons = shopState.weapons.mapIndexed { index: Int, w: Weapon ->
            when (index) {
                selectedIndex -> w.copy(bought = true)
                else -> w
            }
        })*/

        updateSelected(selectedIndex)
        (findViewByName("money") as Text).text = "Money $money"
    }

    container {
        var offset = 0.0
        inventory.weapons.forEachIndexed { index, weapon ->
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
                    sprite(texture = weapon.bitmap) {
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
        text("Money $money") {
            name("money")
        }
        alignBottomToBottomOf(root, 10)
        alignLeftToLeftOf(root, 10)


    }


    updateSelected(0)

}




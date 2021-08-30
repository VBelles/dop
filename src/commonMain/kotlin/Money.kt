import com.soywiz.korge.view.*
import com.soywiz.korinject.injector
import events.EnemyDiedEvent
import events.EventBus

suspend fun Container.money() {
    val inventory = injector().get<Inventory>()
    val assets = injector().get<Assets>()
    val bus = injector().get<EventBus>()

    container {
        val icon = sprite(assets.money) {
            x = 45.0
        }
        val text = text("${inventory.money}") {
            alignRightToLeftOf(icon, 10)
            name("money_indicator")
        }
        scale = 0.85
        x = 595.0
        alignBottomToBottomOf(root, 256)

        bus.register<EnemyDiedEvent> {
            inventory.money += 5
            text.text = "${inventory.money}"
            text.alignRightToLeftOf(icon, 10)
        }
    }



}
import com.soywiz.korge.view.*
import com.soywiz.korinject.injector
import events.EnemyDiedEvent
import events.EventBus

suspend fun Container.money() {
    val inventory = injector().get<Inventory>()
    val assets = injector().get<Assets>()
    val bus = injector().get<EventBus>()

    container {
        text("${inventory.money}") {
            name("money_indicator")
        }
        sprite(assets.money) {
            x = 45.0
        }

        scale = 0.85
        x = 595.0
        alignBottomToBottomOf(root, 256)
    }


    bus.register<EnemyDiedEvent> { event ->
        inventory.money += 50
        (root.findViewByName("money_indicator") as Text).text = "${inventory.money}"
    }
}
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.alignBottomToBottomOf
import com.soywiz.korge.view.container
import com.soywiz.korge.view.sprite
import com.soywiz.korinject.injector
import events.EnemyAttackEvent
import events.EventBus
import events.GameOverEvent

suspend fun Container.hp() {
    val inventory = injector().get<Inventory>()
    val assets = injector().get<Assets>()
    val bus = injector().get<EventBus>()

    fun buildHearts() =
        container {
            val fullHearts = inventory.hp / 4
            var offset = 17.0
            repeat(fullHearts) {
                sprite(assets.hearts["0"]) { x = offset }
                offset += 17
            }
            val remainHeart = inventory.hp % 4.0
            val halfHeart = if (inventory.hp > 0 && remainHeart != 0.0) 1 else 0
            if (halfHeart > 0) {
                sprite(assets.hearts["${4 - remainHeart.toInt()}"]) { x = offset }
                offset += 17
            }
            val emptyHearts = MAX_HP / 4 - fullHearts - halfHeart
            repeat(emptyHearts) {
                sprite(assets.hearts["4"]) { x = offset }
                offset += 17
            }
            scale = 0.5
            x = 670.0
            alignBottomToBottomOf(root, 260)
        }

    var heartsContainer = buildHearts()
    bus.register<EnemyAttackEvent> { event ->
        inventory.hp = (inventory.hp - event.damage.toInt()).coerceAtLeast(0)
        if (inventory.hp == 0) {
            bus.send(GameOverEvent(false))
        }
        heartsContainer.removeFromParent()
        heartsContainer = buildHearts()

    }
}
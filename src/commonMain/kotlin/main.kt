import com.soywiz.korge.Korge
import com.soywiz.korinject.AsyncInjector
import com.soywiz.korinject.withInjector
import com.soywiz.korma.geom.Anchor
import com.soywiz.korma.geom.ScaleMode
import events.EventBus


data class Wave(
    val name: String,
    val duration: Double,
    val enemyRate: List<Long>,
)

fun initWeapons(): List<Weapon> {
    // This is stupid but listOf is not working on js
    val weapons = mutableListOf<Weapon>()
    weapons.add(
        Weapon(
            price = 0,
            name = "Shell",
            description = "Hard, fast and precise shells that hits up to 1 invader\ndealing 2 damage\nCan shoot every 0,5s",
            damage = 2,
            fireRate = 500.0,
            type = Weapon.Type.Shell,
        )
    )

    weapons.add(
        Weapon(
            price = 200,
            name = "Umbrella",
            description = "Like a javelin that go through 3 invaders dealing 1 damage\nto each\nCan shoot every 0.8s",
            damage = 2,
            fireRate = 800.0,
            type = Weapon.Type.Umbrella,
        )
    )
    weapons.add(
        Weapon(
            price = 600,
            name = "Watermelon",
            description = "A overripe watermelon that explodes on contact with\nsand or invaders dealing 2 area damage\nCan shoot every 1,2s",
            damage = 3,
            fireRate = 1200.0,
            type = Weapon.Type.Ball,
        )
    )
    return weapons
}

suspend fun main() = Korge(
    title = "Defence of the Parcel",
    virtualWidth = 800, virtualHeight = 800,
    scaleAnchor = Anchor.BOTTOM_CENTER,
    scaleMode = ScaleMode.COVER
) {
    val weapons = initWeapons()
    val injector = AsyncInjector().apply {
        mapSingleton { Assets() }
        mapInstance(EventBus(this@Korge))
        mapInstance(weapons)
        mapInstance(Inventory(weapons = weapons.take(1), money = 0, score = 0))
    }
    withInjector(injector) {
        /*val bus = injector().get<EventBus>()
        bus.register<GameOverEvent> {
            println("Game over")
            removeChildren()
            mainScene()
        }*/
        mainScene()
    }
}
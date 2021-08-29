import com.soywiz.korge.Korge
import com.soywiz.korinject.AsyncInjector
import com.soywiz.korinject.withInjector
import com.soywiz.korma.geom.Anchor
import com.soywiz.korma.geom.ScaleMode
import events.EventBus


data class Wave(
    val duration: Double,
    val enemyRate: List<Long>,
)

fun initWeapons(): List<Weapon> {
    // This is stupid but listOf is not working on js
    val weapons = mutableListOf<Weapon>()
    weapons.add(
        Weapon(
            price = 0,
            bought = true,
            name = "Shell",
            description = "Hard, fast and precise shells that hits up to 1 invader\ndealing 5 damage\nCan shoot every 0,5s",
            damage = 5.0,
            fireRate = 500.0,
            type = Weapon.Type.Shell,
        )
    )

    weapons.add(
        Weapon(
            price = 7500,
            bought = false,
            name = "Umbrella",
            description = "Like a javelin that go through 3 invaders dealing 5 damage\nto each\nCan shoot every 1s",
            damage = 5.0,
            fireRate = 1000.0,
            type = Weapon.Type.Umbrella,
        )
    )
    weapons.add(
        Weapon(
            price = 21000,
            bought = false,
            name = "Ball",
            description = "A very inflated beach ball that explodes on contact with\nsand or invaders dealing 5 area damage\nCan shoot every 2s",
            damage = 5.0,
            fireRate = 500.0,
            type = Weapon.Type.Ball,
        )
    )
    return weapons
}

suspend fun main() = Korge(
    virtualWidth = 800, virtualHeight = 800,
    scaleAnchor = Anchor.BOTTOM_CENTER,
    scaleMode = ScaleMode.COVER
) {
    val injector = AsyncInjector().apply {
        mapSingleton { Assets() }
        mapInstance(EventBus(this@Korge))
        mapInstance(Inventory(weapons = initWeapons(), money = 25, score = 25))
    }
    withInjector(injector) { mainScene() }
}
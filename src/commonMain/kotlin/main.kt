import com.soywiz.korge.Korge
import com.soywiz.korge.scene.Module
import com.soywiz.korge.scene.Scene
import com.soywiz.korinject.AsyncInjector
import com.soywiz.korma.geom.Anchor
import com.soywiz.korma.geom.ScaleMode
import com.soywiz.korma.geom.SizeInt
import kotlin.reflect.KClass


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

class GameModule : Module() {
    override val title: String = "Defence of the Plot"
    override val mainScene: KClass<out Scene> = GameScene::class
    override val size = SizeInt(800, 800)
    override val scaleAnchor = Anchor.BOTTOM_CENTER
    override val scaleMode = ScaleMode.COVER
    override val windowSize = SizeInt(1280, 720)
    override val icon: String = "sprites/melon.png"

    override suspend fun AsyncInjector.configure() {
        mapPrototype { GameScene() }
    }
}

suspend fun main() = Korge(Korge.Config(GameModule()))
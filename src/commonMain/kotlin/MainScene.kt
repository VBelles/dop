import com.soywiz.klock.DateTime
import com.soywiz.korau.sound.infinitePlaybackTimes
import com.soywiz.korev.MouseButton
import com.soywiz.korge.scene.Scene
import com.soywiz.korge.scene.SceneContainer
import com.soywiz.korge.tiled.tiledMapView
import com.soywiz.korge.view.*
import com.soywiz.korge.view.filter.BlurFilter
import com.soywiz.korim.color.Colors
import com.soywiz.korinject.AsyncInjector
import com.soywiz.korinject.injector
import com.soywiz.korinject.withInjector
import com.soywiz.korma.geom.Point
import events.EventBus
import events.GameOverEvent
import events.NextWaveEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class GameScene : Scene() {

    override suspend fun Container.sceneMain() {
        val injector = AsyncInjector().apply {
            val weapons = initWeapons()
            mapSingleton { Assets() }
            mapInstance(EventBus(this@GameScene))
            mapInstance(weapons)
            mapInstance(Inventory(weapons = weapons.take(1), money = 0, score = 0))
        }
        withInjector(injector) {
            mainScene(sceneContainer)
        }
    }
}

suspend fun Container.mainScene(sceneContainer: SceneContainer) {
    val bus = injector().get<EventBus>()
    val map = injector().get<Assets>().map
    val weapons = injector().get<List<Weapon>>()
    val assets = injector().get<Assets>()

    val objects = map.objectLayers[0]
    val playerSpawn = objects.getByName("player_spawn")!!.let { Point(it.x, it.y) }
    val spawnMax = objects.getByName("spawn_max")!!
    val spawnMin = objects.getByName("spawn_min")!!
    val baseX = objects.getByName("base")!!.x

    val blurFilter = BlurFilter()

    val scenario = container {
        tiledMapView(map)
        player(playerSpawn)
        hp()
        money()
        text("") { name("weekday") }
    }

    val shop = container {
        name("shop")
        shop()
        visible = false
    }


    scenario.addFilter(blurFilter)

    val intro = intro(bus)

    val music = assets.music.play(infinitePlaybackTimes).apply { volume = 0.4 }

    bus.waitEvent<NextWaveEvent>()
    intro.removeFromParent()
    scenario.removeFilter(blurFilter)

    delay(1000)

    val waves = mutableListOf<Wave>()
    waves.add(Wave("Monday", 30000.0, listOf(1500, 1200, 1100)))
    waves.add(Wave("Tuesday", 32000.0, listOf(1300, 1100, 1100)))
    waves.add(Wave("Wednesday", 40000.0, listOf(1300, 1000, 1000)))
    waves.add(Wave("Thursday", 45000.0, listOf(1100, 1000, 1000)))
    waves.add(Wave("Friday", 50000.0, listOf(800, 400, 200)))
    waves.add(Wave("Saturday", 55000.0, listOf(700, 300, 200)))
    waves.add(Wave("Sunday", 60000.0, listOf(600, 300, 200)))


    lateinit var lastWave: Wave
    var gameOver = false
    bus.register<GameOverEvent> {
        if (!gameOver) {
            gameOver = true
            scenario.addFilter(blurFilter)
            gameOver(lastWave.name) {
                sceneContainer.changeTo<GameScene>()
            }
        }
    }


    waves.forEach { wave ->
        lastWave = wave
        (root.findViewByName("weekday") as Text).apply {
            text = wave.name
            alignBottomToBottomOf(root, 120)
            centerXBetween(585, 765)
        }
        assets.waveSound.play()
        delay(1000)
        var now = DateTime.nowUnix()
        val start = now
        val end = start + wave.duration
        while (DateTime.nowUnix() < end) {
            val progress = (now - start) / (end - start)
            val phase = (progress * wave.enemyRate.size).toInt()
            delay(wave.enemyRate[phase])
            now = DateTime.nowUnix()
            val position = Point(spawnMax.x, Random.nextDouble(spawnMax.y, spawnMin.y))
            scenario.enemy(position, baseX, EnemyType.values().random(), weapons.last())
            if (gameOver) return
        }

        bus.send(events.ClearWaveEvent)
        scenario.addFilter(blurFilter)
        shop.visible = true

        music.volume = 0.1

        bus.waitEvent<NextWaveEvent>()

        shop.visible = false
        scenario.removeFilter(blurFilter)

        music.volume = 0.4
    }
}

fun Container.intro(bus: EventBus) = container {
    roundRect(
        width = 380.0,
        height = 100.0,
        rx = 15.0,
        ry = 15.0,
        fill = Colors.BLACK.withA(70),
    )
    centerXOnStage()
    text(
        "You woke up very early to take a nice parceled place\non the beach, doesn't matter what will come, you will\nprotect it",
        color = Colors.WHITE
    ) {
        x = 10.0
        y = 10.0
    }
    text("*Click to continue*", color = Colors.WHITE) {
        x = 10.0
        y = 10.0
        centerOn(parent!!)
        alignBottomToBottomOf(parent!!, 10)
    }
    alignBottomToBottomOf(root, 200)

    addUpdaterWithViews { views, _ ->
        if (views.input.mouseButtonPressed(MouseButton.LEFT)) {
            bus.send(NextWaveEvent)
        }
    }

}

suspend fun Container.gameOver(weekday: String, restart: suspend () -> Unit) = container {
    roundRect(
        width = 380.0,
        height = 60.0,
        rx = 15.0,
        ry = 15.0,
        fill = Colors.BLACK.withA(70),
    )
    centerXOnStage()
    alignBottomToBottomOf(root, 200)

    text(
        "You kept the best place of the beach until $weekday",
        color = Colors.WHITE
    ) {
        centerXOn(parent!!)
        alignTopToTopOf(parent!!, 10)
    }
    text(
        "*Click to try again*",
        color = Colors.WHITE
    ) {
        centerXOn(parent!!)
        alignBottomToBottomOf(parent!!, 10)
    }
    delay(1000)
    addUpdaterWithViews { views, _ ->
        if (views.input.mouseButtonPressed(MouseButton.LEFT) || views.input.mouseButtonPressed(MouseButton.RIGHT)) {
            views.launch { restart() }
        }
    }
}

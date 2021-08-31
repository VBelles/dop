import com.soywiz.klock.DateTime
import com.soywiz.klock.TimeSpan
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
import events.ClearWaveEvent
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

    var music = assets.music.play().apply { volume = 0.4 }

    addUpdaterWithViews { views, _ ->
        views.launch {
            if (music.current >= music.total - TimeSpan(5000.0)) {
                val volume = music.volume
                music.stop()
                music = assets.music.play()
                music.volume = volume
            }
        }
    }

    bus.waitEvent<NextWaveEvent>()
    intro.removeFromParent()
    scenario.removeFilter(blurFilter)

    delay(1000)

    val waves = mutableListOf<Wave>()
    waves.add(Wave("Monday", 30000.0, listOf(1500, 1200, 1100)))
    waves.add(Wave("Tuesday", 32000.0, listOf(1300, 1100, 1100)))
    waves.add(Wave("Wednesday", 40000.0, listOf(1300, 1000, 1000)))
    waves.add(Wave("Thursday", 45000.0, listOf(1100, 900, 800)))
    waves.add(Wave("Friday", 50000.0, listOf(1100, 800, 700)))
    waves.add(Wave("Saturday", 55000.0, listOf(1000, 800, 600)))
    waves.add(Wave("Sunday", 60000.0, listOf(900, 800, 500)))



    lateinit var lastWave: Wave
    var gameOver = false
    bus.register<GameOverEvent> { event ->
        bus.send(ClearWaveEvent)
        if (!gameOver) {
            gameOver = true
            music.volume = 0.1
            scenario.addFilter(blurFilter)
            gameOver(event.victory, assets, lastWave.name) {
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
        assets.waveSound.playFixed()
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

        if (wave === waves.last()) {
            // Victory!
            bus.send(GameOverEvent(true))
            return
        }

        bus.send(ClearWaveEvent)
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
    val background = roundRect(
        width = 380.0,
        height = 170.0,
        rx = 15.0,
        ry = 15.0,
        fill = Colors.BLACK.withA(70),
    ) {
        alignBottomToBottomOf(root, 130)
        centerXOn(root)
    }
    val title = text(
        "DEFENCE OF THE PLOT",
        color = Colors.WHITE
    ) {
        scale = 2.0
        centerXOn(background)
        alignTopToTopOf(background, 20)
    }
    text(
        "You woke up very early to take a nice plot on the\nbeach, doesn't matter what will come, you will protect\nit and enjoy your holidays",
        color = Colors.WHITE
    ) {
        alignLeftToLeftOf(background, 20)
        alignTopToBottomOf(title, 10)
    }
    text("*Click to continue*", color = Colors.WHITE) {
        centerXOn(background)
        alignBottomToBottomOf(background, 20)
    }

    addUpdaterWithViews { views, _ ->
        if (views.input.mouseButtonPressed(MouseButton.LEFT)) {
            bus.send(NextWaveEvent)
        }
    }

}

suspend fun Container.gameOver(victory: Boolean, assets: Assets, weekday: String, restart: suspend () -> Unit) =
    container {
        roundRect(
            width = 380.0,
            height = 80.0,
            rx = 15.0,
            ry = 15.0,
            fill = Colors.BLACK.withA(70),
        )
        centerXOnStage()
        alignBottomToBottomOf(root, 200)

        val text = when {
            victory -> "Congratulations! You kept the best place of the beach\n all your holidays"
            else -> "You kept the best place of the beach until $weekday"
        }
        text(text, color = Colors.WHITE) {
            centerXOn(parent!!)
            alignTopToTopOf(parent!!, 10)
        }
        text("*Click to ${if (victory) "play" else "try"} again*", color = Colors.WHITE) {
            centerXOn(parent!!)
            alignBottomToBottomOf(parent!!, 10)
        }
        if (victory) {
            assets.winSound.playFixed()
        } else {
            assets.gameOver.playFixed()
        }
        delay(1000)
        addUpdaterWithViews { views, _ ->
            if (views.input.mouseButtonPressed(MouseButton.LEFT) || views.input.mouseButtonPressed(MouseButton.RIGHT)) {
                views.launch { restart() }
            }
        }
    }

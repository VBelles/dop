import com.soywiz.klock.DateTime
import com.soywiz.korau.sound.infinitePlaybackTimes
import com.soywiz.korev.MouseButton
import com.soywiz.korge.tiled.tiledMapView
import com.soywiz.korge.view.*
import com.soywiz.korge.view.filter.BlurFilter
import com.soywiz.korim.color.Colors
import com.soywiz.korinject.injector
import com.soywiz.korma.geom.Point
import events.EventBus
import events.NextWaveEvent
import kotlinx.coroutines.delay

suspend fun Stage.mainScene() {
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
    }

    val shop = container {
        name("shop")
        shop()
        visible = false
    }

    val music = assets.music.play(infinitePlaybackTimes).apply { volume = 0.4 }

    scenario.addFilter(blurFilter)

    val intro = container {
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

        addUpdater {
            if (input.mouseButtonPressed(MouseButton.LEFT)) {
                bus.send(NextWaveEvent)
            }
        }
    }

    bus.waitEvent<NextWaveEvent>()
    intro.removeFromParent()
    scenario.removeFilter(blurFilter)

    delay(1000)

    val waves = mutableListOf<Wave>()
    waves.add(Wave(30000.0, listOf(1500L, 1000L, 800L)))
    waves.add(Wave(35000.0, listOf(1500L, 1000L, 800L)))
    waves.add(Wave(40000.0, listOf(1500L, 1000L, 800L)))
    waves.add(Wave(45000.0, listOf(1500L, 1000L, 800L)))
    waves.add(Wave(50000.0, listOf(1500L, 1000L, 800L)))
    waves.add(Wave(55000.0, listOf(1500L, 1000L, 800L)))
    waves.add(Wave(60000.0, listOf(1500L, 1000L, 800L)))


    waves.forEach { wave ->
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
            val position = Point(spawnMax.x, kotlin.random.Random.nextDouble(spawnMax.y, spawnMin.y))
            scenario.enemy(position, baseX, EnemyType.values().random(), weapons.last())
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
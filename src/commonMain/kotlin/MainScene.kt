import com.soywiz.korge.tiled.tiledMapView
import com.soywiz.korge.view.Stage
import com.soywiz.korge.view.container
import com.soywiz.korge.view.filter.BlurFilter
import com.soywiz.korge.view.name
import com.soywiz.korinject.injector
import com.soywiz.korma.geom.Point
import events.EventBus
import events.NextWaveEvent
import kotlinx.coroutines.delay

suspend fun Stage.mainScene() {
    val bus = injector().get<EventBus>()
    val map = injector().get<Assets>().map
    val weapons = injector().get<List<Weapon>>()

    val objects = map.objectLayers[0]
    val playerSpawn = objects.getByName("player_spawn")!!.let { Point(it.x, it.y) }
    val spawnMax = objects.getByName("spawn_max")!!
    val spawnMin = objects.getByName("spawn_min")!!
    val baseX = objects.getByName("base")!!.x

    val scenario = container {
        tiledMapView(map)
        player(playerSpawn)
    }

    val shop = container {
        name("shop")
        shop()
        visible = false
    }

    val blurFilter = BlurFilter()

    val waves = mutableListOf<Wave>()
    waves.add(Wave(30000.0, listOf(1500L, 1000L, 800L)))
    waves.add(Wave(35000.0, listOf(1500L, 1000L, 800L)))
    waves.add(Wave(40000.0, listOf(1500L, 1000L, 800L)))
    waves.add(Wave(45000.0, listOf(1500L, 1000L, 800L)))
    waves.add(Wave(50000.0, listOf(1500L, 1000L, 800L)))
    waves.add(Wave(55000.0, listOf(1500L, 1000L, 800L)))
    waves.add(Wave(60000.0, listOf(1500L, 1000L, 800L)))

    waves.forEach { wave ->
        var now = com.soywiz.klock.DateTime.nowUnix()
        val start = now
        val end = start + wave.duration
        while (com.soywiz.klock.DateTime.nowUnix() < end) {
            val progress = (now - start) / (end - start)
            val phase = (progress * wave.enemyRate.size).toInt()
            delay(wave.enemyRate[phase])
            now = com.soywiz.klock.DateTime.nowUnix()
            val position = Point(spawnMax.x, kotlin.random.Random.nextDouble(spawnMax.y, spawnMin.y))
            scenario.enemy(position, baseX, EnemyType.values().random(), weapons.last())
        }

        bus.send(events.ClearWaveEvent)
        scenario.addFilter(blurFilter)
        shop.visible = true

        bus.waitEvent<NextWaveEvent>()

        shop.visible = false
        scenario.removeFilter(blurFilter)
    }
}
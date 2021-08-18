import com.soywiz.klock.TimeSpan
import com.soywiz.korge.Korge
import com.soywiz.korge.tiled.readTiledMap
import com.soywiz.korge.tiled.tiledMapView
import com.soywiz.korge.view.getSpriteAnimation
import com.soywiz.korim.atlas.readAtlas
import com.soywiz.korim.format.readBitmap
import com.soywiz.korio.file.std.resourcesVfs
import com.soywiz.korma.geom.Anchor
import com.soywiz.korma.geom.Point
import com.soywiz.korma.geom.ScaleMode
import kotlinx.coroutines.delay


suspend fun main() = Korge(
    virtualWidth = 800, virtualHeight = 800,
    scaleAnchor = Anchor.BOTTOM_CENTER,
    scaleMode = ScaleMode.COVER
) {
    val bus = EventBus(this)

    val map = resourcesVfs["dop.tmx"].readTiledMap()

    val objects = map.objectLayers[0]
    val playerSpawn = objects.getByName("player_spawn")!!.let { Point(it.x, it.y) }
    val spawnMax = objects.getByName("spawn_max")!!
    val spawnMin = objects.getByName("spawn_min")!!
    val baseX = objects.getByName("base")!!.x

    val weapon = resourcesVfs["sprites/shell.png"].readBitmap()

    tiledMapView(map)

    val playerAtlas = resourcesVfs["sprites/player/player_sheet.xml"].readAtlas()
    player(bus, playerSpawn, weapon, playerAtlas)

    val zombieAtlas = resourcesVfs["sprites/zombie/zombie_sheet.xml"].readAtlas()
    val runAnimation = zombieAtlas.getSpriteAnimation(prefix = "run", TimeSpan(120.0))
    while (true) {
        enemy(bus, spawnMax.x, spawnMin.y, spawnMax.y, baseX, runAnimation)
        delay(500)
    }
}
import com.soywiz.korge.Korge
import com.soywiz.korge.tiled.readTiledMap
import com.soywiz.korge.tiled.tiledMapView
import com.soywiz.korge.view.container
import com.soywiz.korim.atlas.readAtlas
import com.soywiz.korim.format.readBitmap
import com.soywiz.korio.file.std.resourcesVfs
import com.soywiz.korma.geom.Anchor
import com.soywiz.korma.geom.Point
import com.soywiz.korma.geom.ScaleMode
import events.EventBus
import kotlinx.coroutines.delay


suspend fun initWeapons(): List<Weapon> {
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
            bitmap = resourcesVfs["sprites/shell.png"].readBitmap(),
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
            bitmap = resourcesVfs["sprites/beach_umbrella.png"].readBitmap(),
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
            bitmap = resourcesVfs["sprites/beach_ball.png"].readBitmap(),
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
    val bus = EventBus(this)

    val map = resourcesVfs["dop.tmx"].readTiledMap()

    val objects = map.objectLayers[0]
    val playerSpawn = objects.getByName("player_spawn")!!.let { Point(it.x, it.y) }
    val spawnMax = objects.getByName("spawn_max")!!
    val spawnMin = objects.getByName("spawn_min")!!
    val baseX = objects.getByName("base")!!.x

    val inventory = Inventory(weapons = initWeapons(), money = 25, score = 25)

    container {
        tiledMapView(map)
        val playerAtlas = resourcesVfs["sprites/player/player_sheet.xml"].readAtlas()
        player(bus, playerSpawn, playerAtlas, inventory)
        //addFilter(BlurFilter())
    }

    //shop(bus, inventory)

    val zombieAtlas = resourcesVfs["sprites/zombie/zombie_sheet.xml"].readAtlas()
    while (true) {
        enemy(bus, spawnMax.x, spawnMin.y, spawnMax.y, baseX, zombieAtlas)
        delay(500)
    }
}
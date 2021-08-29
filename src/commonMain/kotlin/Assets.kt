import com.soywiz.korge.tiled.TiledMap
import com.soywiz.korge.tiled.readTiledMap
import com.soywiz.korim.atlas.Atlas
import com.soywiz.korim.atlas.readAtlas
import com.soywiz.korim.bitmap.Bitmap
import com.soywiz.korim.format.readBitmap
import com.soywiz.korinject.AsyncDependency
import com.soywiz.korio.file.std.resourcesVfs

class Assets : AsyncDependency {

    lateinit var map: TiledMap
    lateinit var invaderAtlas: Atlas
    lateinit var playerAtlas: Atlas
    lateinit var explosionAtlas: Atlas
    lateinit var shellBitmap: Bitmap
    lateinit var umbrellaBitmap: Bitmap
    lateinit var ballBitmap: Bitmap

    fun getWeaponBitmap(weapon: Weapon): Bitmap {
        return when (weapon.type) {
            Weapon.Type.Shell -> shellBitmap
            Weapon.Type.Umbrella -> umbrellaBitmap
            Weapon.Type.Ball -> ballBitmap
        }
    }

    override suspend fun init() {
        map = resourcesVfs["dop.tmx"].readTiledMap()
        playerAtlas = resourcesVfs["sprites/player/player_sheet.xml"].readAtlas()
        invaderAtlas = resourcesVfs["sprites/zombie/zombie_sheet.xml"].readAtlas()
        explosionAtlas = resourcesVfs["sprites/explosion.xml"].readAtlas()
        shellBitmap = resourcesVfs["sprites/shell.png"].readBitmap()
        umbrellaBitmap = resourcesVfs["sprites/beach_umbrella.png"].readBitmap()
        ballBitmap = resourcesVfs["sprites/beach_ball.png"].readBitmap()
    }

}
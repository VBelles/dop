import com.soywiz.korau.sound.Sound
import com.soywiz.korau.sound.readMusic
import com.soywiz.korau.sound.readSound
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
    lateinit var hearts: Atlas
    lateinit var money: Bitmap

    lateinit var hitSound: Sound
    lateinit var throwSound: Sound
    lateinit var explosionSound: Sound
    lateinit var music: Sound
    lateinit var step1Sound: Sound
    lateinit var step2Sound: Sound
    lateinit var waveSound: Sound
    lateinit var clickSound: Sound
    lateinit var clickErrorSound: Sound
    lateinit var buySound: Sound
    lateinit var earnMoneySound: Sound

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
        hearts = resourcesVfs["sprites/heart_sheet.xml"].readAtlas()
        money = resourcesVfs["sprites/money.png"].readBitmap()

        hitSound = resourcesVfs["sound/hit.mp3"].readSound().apply { volume = 0.5 }
        throwSound = resourcesVfs["sound/throw.mp3"].readSound()
        explosionSound = resourcesVfs["sound/explosion.mp3"].readSound().apply { volume = 0.5 }
        music = resourcesVfs["sound/music.mp3"].readMusic()
        step1Sound = resourcesVfs["sound/step1.mp3"].readSound().apply {
            volume = 0.05
        }
        step2Sound = resourcesVfs["sound/step2.mp3"].readSound().apply {
            volume = 0.05
        }
        waveSound = resourcesVfs["sound/wave.mp3"].readSound().apply { volume = 0.3 }
        clickSound = resourcesVfs["sound/click.mp3"].readSound().apply { volume = 0.6 }
        clickErrorSound = resourcesVfs["sound/click_error.mp3"].readSound().apply { volume = 0.15 }
        buySound = resourcesVfs["sound/buy.mp3"].readSound()
        earnMoneySound = resourcesVfs["sound/earn_money.mp3"].readSound()
    }

}
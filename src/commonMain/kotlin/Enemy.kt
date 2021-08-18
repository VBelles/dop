import com.soywiz.korge.view.*
import kotlin.random.Random


fun Stage.enemy(
    bus: EventBus,
    spawnX: Double,
    spawnMinY: Double,
    spawnMaxY: Double,
    baseX: Double,
    runAnimation: SpriteAnimation
) {
    val speed = 50f
    var hp = 2
    sprite(runAnimation) {
        addProp("enemy", true)
        scale = 0.3
        position(spawnX, Random.nextDouble(spawnMaxY, spawnMinY - scaledHeight))
        playAnimationLooped()
        addUpdater { delta ->
            if (x < baseX - scaledWidth) {
                x += delta.seconds * speed
            }
        }
        bus.register<BulletHitEvent> { event ->
            if (event.target === this) {
                hp--
                if (hp <= 0) {
                    removeFromParent()
                }
            }
        }
    }
}



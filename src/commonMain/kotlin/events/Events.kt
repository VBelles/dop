package events

import Weapon
import com.soywiz.korge.view.View

data class BulletHitEvent(val target: View, val damage: Int)

data class EnemyAttackEvent(val damage: Int)

object EnemyDiedEvent

object ClearWaveEvent

object NextWaveEvent

data class GameOverEvent(val victory: Boolean)

data class WeaponBoughtEvent(val weapon: Weapon)


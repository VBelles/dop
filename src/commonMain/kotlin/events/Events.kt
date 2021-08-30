package events

import Weapon
import com.soywiz.korge.view.View

data class BulletHitEvent(val target: View, val damage: Double)

data class EnemyAttackEvent(val damage: Double)

object EnemyDiedEvent

object ClearWaveEvent

object NextWaveEvent

object GameOverEvent

data class WeaponBoughtEvent(val weapon: Weapon)


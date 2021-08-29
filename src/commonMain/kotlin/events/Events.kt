package events

import Weapon
import com.soywiz.korge.view.View

data class BulletHitEvent(val target: View, val damage: Double)

data class EnemyAttackEvent(val damage: Double)

object ClearWaveEvent

object NextWaveEvent

data class WeaponBoughtEvent(val weapon: Weapon)
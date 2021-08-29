package events

import Weapon
import com.soywiz.korge.view.View

data class BulletHitEvent(val target: View, val damage: Double)

data class EnemyAttackEvent(val damage: Double)

data class ChangeWeapon(val weapon: Weapon)

object ClearWaveEvent

object NextWaveEvent
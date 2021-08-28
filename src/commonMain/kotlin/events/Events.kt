package events

import Weapon
import com.soywiz.korge.view.View

data class BulletHitEvent(val target: View, val damage: Double)

data class EnemyAttackEvent(val damage: Int)

data class ChangeWeapon(val weapon: Weapon)
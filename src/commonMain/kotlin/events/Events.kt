package events

import com.soywiz.korge.view.View

data class BulletHitEvent(val target: View)

data class EnemyAttackEvent(val damage: Int)
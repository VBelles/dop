data class Weapon(
    val price: Int,
    val name: String,
    val description: String,
    val damage: Int,
    val fireRate: Double,
    val type: Type
) {
    enum class Type {
        Shell, Umbrella, Ball,
    }
}

data class Inventory(
    var weapons: List<Weapon>,
    var money: Int,
    var hp: Int = MAX_HP,
    var score: Int,
)

const val MAX_HP = 32
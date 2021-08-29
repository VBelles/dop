data class Weapon(
    val price: Int,
    val name: String,
    val description: String,
    val damage: Double,
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
    var score: Int,
)
import com.soywiz.korim.bitmap.Bitmap

data class Weapon(
    val price: Int,
    val bought: Boolean,
    val name: String,
    val description: String,
    val damage: Double,
    val fireRate: Double,
    val bitmap: Bitmap,
    val type: Type
) {
    enum class Type {
        Shell, Umbrella, Ball,
    }
}

data class Inventory(
    val weapons: List<Weapon>,
    val money: Int,
    val score: Int,
)
import kotlinx.serialization.Serializable

@Serializable
data class CoinPosition(val x: Double, val y: Double, val z: Double) {
  companion object{
    val default = CoinPosition(0.0, 0.0, 0.0)
  }
}

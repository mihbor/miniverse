import kotlinx.serialization.json.decodeFromDynamic
import minima.MDS
import minima.json

suspend fun getCoins(tokenId: String? = null, address: String? = null, coinId: String? = null, sendable: Boolean): List<Coin> {
  val coinSimple = MDS.cmd("coins${tokenId?.let{" tokenid:$it"} ?:""}${address?.let{" address:$it"} ?:""}${coinId?.let{" coinid:$it"} ?:""} sendable:$sendable")
  val coins = json.decodeFromDynamic<Array<Coin>>(coinSimple.response)
  return coins.sortedWith(compareBy({ it.tokenid }, { it.amount }))
}
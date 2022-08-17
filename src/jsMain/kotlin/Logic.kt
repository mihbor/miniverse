import kotlinx.serialization.json.decodeFromDynamic
import minima.MDS
import minima.json

suspend fun coinsForAddress(address: String): List<Coin> {
  val coinSimple = MDS.cmd("coins address:$address")
  val coins = json.decodeFromDynamic<Array<Coin>>(coinSimple.response)
  return coins.sortedWith(compareBy({ it.tokenid }, { it.amount }))
}
import kotlinx.serialization.json.decodeFromDynamic
import minima.MDS
import minima.json

const val MINIDAPP = "miniverse"

fun ByteArray.toHex(): String = asUByteArray().joinToString("") { it.toString(radix = 16).padStart(2, '0') }

suspend fun getCoins(tokenId: String? = null, address: String? = null, coinId: String? = null, sendable: Boolean): List<Coin> {
  val coinSimple = MDS.cmd("coins${tokenId?.let{" tokenid:$it"} ?:""}${address?.let{" address:$it"} ?:""}${coinId?.let{" coinid:$it"} ?:""} sendable:$sendable")
  val coins = json.decodeFromDynamic<Array<Coin>>(coinSimple.response)
  return coins.sortedWith(compareBy({ it.tokenid }, { it.amount }))
}

suspend fun getContacts(): List<Contact> {
  val maxcontacts = MDS.cmd("maxcontacts")
  return json.decodeFromDynamic(maxcontacts.response.contacts)
}

suspend fun addContact(maxiAddress: String): Unit {
  val maxcontacts = MDS.cmd("maxcontacts action:add contact:$maxiAddress")
}

suspend fun sendMessage(publicKey: String, text: String): Boolean {
  val hex = text.encodeToByteArray().toHex()
  val maxima = MDS.cmd("maxima action:send application:$MINIDAPP publickey:$publicKey data:$hex")
  return maxima.status == true && maxima.response.delivered == true
}

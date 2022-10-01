import kotlinx.coroutines.launch
import kotlinx.serialization.json.decodeFromDynamic
import minima.Coin
import minima.Contact
import minima.MDS
import minima.json

const val MINIDAPP = "miniverse"

fun ByteArray.toHex(): String = asUByteArray().joinToString("") { it.toString(radix = 16).padStart(2, '0') }
fun String.decodeHex(): ByteArray {
  check(length % 2 == 0) { "Must have an even length" }
  
  return chunked(2)
    .map { it.toInt(16).toByte() }
    .toByteArray()
}

suspend fun getCoins(tokenId: String? = null, address: String? = null, coinId: String? = null, sendable: Boolean): List<Coin> {
  val coinSimple = MDS.cmd("coins${tokenId?.let{" tokenid:$it"} ?:""}${address?.let{" address:$it"} ?:""}${coinId?.let{" coinid:$it"} ?:""} sendable:$sendable")
  val coins = json.decodeFromDynamic<Array<Coin>>(coinSimple.response)
  return coins.sortedWith(compareBy({ it.tokenid }, { it.amount }))
}

suspend fun exportCoin(coinId: String): String {
  val coinexport = MDS.cmd("coinexport coinid:$coinId")
  return coinexport.response
}

suspend fun importCoin(data: String) {
  val coinimport = MDS.cmd("coinimport data:$data")
}

suspend fun getContacts(): List<Contact> {
  val maxcontacts = MDS.cmd("maxcontacts")
  return json.decodeFromDynamic(maxcontacts.response.contacts)
}

suspend fun addContact(maxiAddress: String): Contact? {
  val maxcontacts = MDS.cmd("maxcontacts action:add contact:$maxiAddress")
  return if (maxcontacts.status == true)
    getContacts().first{ it.currentaddress == maxiAddress }
  else null
}

suspend fun sendMessage(publicKey: String, text: String): Boolean {
  val hex = "0x" + text.encodeToByteArray().toHex()
  val maxima = MDS.cmd("maxima action:send application:$MINIDAPP publickey:$publicKey data:$hex")
  console.log("sent: $text")
  return maxima.status == true && maxima.response.delivered == true
}

fun connect(maxiAddress: String, miniAddress: String) {
  scope.launch {
    val contacts = getContacts()
    val contact = contacts.firstOrNull { it.currentaddress == maxiAddress } ?: addContact(maxiAddress)
    console.log("contact:", contact)
    val delivered = sendMessage(contact!!.publickey, "inventory-request;$miniAddress")
    console.log("delivered:", delivered)
  }
}

suspend fun getCoinPosition(coinId: String): CoinPosition? {
  val sql = MDS.sql("SELECT * FROM coinpos WHERE coinid = '$coinId';")
  val rows = sql.rows as Array<dynamic>
  console.log("rows:", rows)
  return rows.takeIf { it.size == 1 }?.let{
    CoinPosition(
      (it[0].X as String).toDouble(),
      (it[0].Y as String).toDouble(),
      (it[0].Z as String).toDouble()
    )
  }
}

suspend fun updateCoinPosition(coinId: String, position: CoinPosition) {
  
  MDS.sql("""INSERT INTO coinpos VALUES ('$coinId', ${position.x}, ${position.y}, ${position.z})
      ON DUPLICATE KEY UPDATE x = ${position.x}, y = ${position.y}, z = ${position.z};""")
}
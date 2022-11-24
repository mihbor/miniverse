import kotlinx.coroutines.launch
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import ltd.mbor.minimak.*

const val MINIDAPP = "miniverse"

external fun decodeURIComponent(encodedURI: String): String

fun connect(maxiAddress: String, miniAddress: String) {
  scope.launch {
    val contacts = MDS.getContacts()
    val contact = contacts.firstOrNull { it.currentAddress == maxiAddress } ?: MDS.addContact(maxiAddress)
    console.log("contact:", contact)
    val delivered = MDS.sendMessage(MINIDAPP, contact!!.publicKey, "inventory-request;$miniAddress")
    console.log("delivered:", delivered)
  }
}

suspend fun getCoinPosition(coinId: String): CoinPosition? {
  val sql = MDS.sql("SELECT * FROM coinpos WHERE coinid = '$coinId';")!!
  val rows = sql.jsonObject["rows"]!!.jsonArray
  console.log("rows:", rows)
  return rows.takeIf { it.size == 1 }?.let{
    CoinPosition(
      it[0].jsonString("X")!!.toDouble(),
      it[0].jsonString("Y")!!.toDouble(),
      it[0].jsonString("Z")!!.toDouble()
    )
  }
}

suspend fun updateCoinPosition(coinId: String, position: CoinPosition) {
  
  MDS.sql("""INSERT INTO coinpos VALUES ('$coinId', ${position.x}, ${position.y}, ${position.z})
      ON DUPLICATE KEY UPDATE x = ${position.x}, y = ${position.y}, z = ${position.z};""")
}
package minima

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

data class Balance(
  val tokenid: String,
  val token: TokenDescriptor?,
//  val total: String,
  val confirmed: BigDecimal,
  val unconfirmed: BigDecimal,
  val sendable: BigDecimal,
  val coins: Int
)

@Serializable
data class TokenDescriptor(
  val name: String,
  val description: String?,
  val url: String?
)

@Serializable
data class Coin(
  val address: String,
  @Contextual
  val amount: BigDecimal,
  @Contextual
  val tokenamount: BigDecimal? = amount,
  val coinid: String,
  val storestate: Boolean,
  val tokenid: String
)

@Serializable
data class Token(
  val tokenid: String,
  val token: String,
  val total: String,
  val decimals: String,
  val description: String? = null,
  val icon: String? = null,
  val proof: String? = null,
  val script: String? = null,
  val coinid: String? = null,
  val totalamount: String? = null,
  val scale: String? = null
)

@Serializable
data class Contact(
  val id: Int,
  val publickey: String,
  val currentaddress: String,
  val myaddress: String,
  val lastseen: Int,
  val date: String,
  val chaintip: String,
  val samechain: Boolean,
  val extradata: ExtraData
)

@Serializable
data class ExtraData(
  val name: String,
  val minimaaddress: String,
  val topblock: String,
  val checkblock: String,
  val checkhash: String,
  val mls: String
)
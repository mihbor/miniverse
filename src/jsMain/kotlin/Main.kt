import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import ltd.mbor.minimak.*
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Br
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.TextInput
import org.jetbrains.compose.web.renderComposableInBody
import org.w3c.dom.Window
import three.js.*
import three.mesh.ui.*
import kotlin.math.PI

val scope = MainScope()

external interface Options {
  var passive: Boolean
}

val Window.aspectRatio get() = innerWidth.toDouble() / innerHeight

val camera = PerspectiveCamera(60, window.aspectRatio, 0.5, 2e9)

var minimaTex: Texture? = null

operator fun Number.minus(other: Number) = toDouble() - other.toDouble()
operator fun Number.plus(other: Number) = toDouble() + other.toDouble()
operator fun Number.times(other: Number) = toDouble() * other.toDouble()
operator fun Number.div(other: Number) = toDouble() / other.toDouble()
operator fun Number.compareTo(other: Number) = toDouble().compareTo(other.toDouble())

fun square(base: Int) = base * base

val renderer = WebGLRenderer((js("{}").unsafeCast<WebGLRendererParameters>()).apply{
  antialias = false
  logarithmicDepthBuffer = false
}).apply {
//    document.body?.appendChild( VRButton.createButton(this) )
  document.body?.appendChild(domElement)
  setSize(window.innerWidth-20, window.innerHeight-40)
  setPixelRatio(window.devicePixelRatio)
  xr.enabled = false
}

val coins = mutableStateMapOf<Object3D, Coin>()
val scene = createScene()

var focused: Object3D? = null

val raycaster = Raycaster().apply {
  far = 2e8
}

object form {
  
  //Return the GET parameter by scraping the location..
  fun getParams(parameterName: String): String?{
    var result: String? = null
    val items = window.location.search.substring(1).split("&");
    for (item in items) {
      val tmp = item.split("=");
      //console.log("TMP:"+tmp);
      if (tmp[0] == parameterName) result = decodeURIComponent(tmp[1])
    }
    return result
  }
}

suspend fun main() {
  var miniAddress by mutableStateOf("")
  
  MDS.init(form.getParams("uid") ?: "0x00", window.location.hostname, 9004) { msg: dynamic ->
    val event = msg.event
    when(event) {
      "inited" -> {
        if (MDS.logging) console.log("Connected to Minima.")
        scope.launch {
          //DROP TABLE IF EXISTS coinpos;
          MDS.sql("""CREATE TABLE IF NOT EXISTS coinpos(
            coinid VARCHAR(69) PRIMARY KEY NOT NULL,
            x DECIMAL(20,10),
            y DECIMAL(20,10),
            z DECIMAL(20,10)
          );""".trimMargin())
        }
      }
      "MAXIMA" -> if (msg.data.application == MINIDAPP) {
        val data = (msg.data.data as String).substring(2).decodeHex().decodeToString()
        val sender = msg.data.from as String
        console.log("received: $data from $sender")
        val splits = data.split(";")
        when (splits[0]) {
          "inventory-request" -> {
            val address = splits[1]
            scope.launch {
              val coins = MDS.getCoins(address = address, sendable = true).map {
                val pos = getCoinPosition(it.coinId) ?: CoinPosition.default
                "${it.coinId};${MDS.exportCoin(it.coinId)};${pos.x};${pos.y};${pos.z}"
              }
              MDS.sendMessage(MINIDAPP, sender, "inventory-response;${coins.size};${coins.joinToString(";")}")
            }
          }
          "inventory-response" -> {
            val size = splits[1].toInt()
            check(splits.size == size * 5 + 2) { "Expected message elements ${size * 5 + 2}, got ${splits.size}" }
            scope.launch {
              val coins = (0 until size).map { index ->
                val coinid = splits[index*5 + 2]
                MDS.importCoin(splits[index*5 + 3])
                updateCoinPosition(coinid, CoinPosition(splits[index*5 + 4].toDouble(), splits[index*5 + 5].toDouble(), splits[index*5 + 6].toDouble()))
              }
              reload(miniAddress)
            }
          }
        }
      }
    }
  }
  window.onresize = {
    camera.aspect = window.aspectRatio
    camera.updateProjectionMatrix()
    
    renderer.setSize(window.innerWidth, window.innerHeight-4)
  }
  document.addEventListener("click", ::clickHandler, false)
  menu = createMenuDisplay()
  
  registerListeners()
  
  animate()
  
  var maxContact by mutableStateOf("")
  renderComposableInBody {
    Text("Minima address:")
    TextInput(miniAddress) {
      style {
        width(500.px)
      }
      onInput {
        miniAddress = it.value
      }
    }
    Button({
      onClick {
        reload(miniAddress)
        if (maxContact.isNotBlank()) connect(maxContact, miniAddress)
      }
      if (miniAddress.isBlank()) disabled()
    }) {
      Text("Go!")
    }
    Br()
    Text("Maxima contact:")
    TextInput(maxContact) {
      style {
        width(800.px)
      }
      onInput {
        maxContact = it.value
      }
    }
    Button({
      onClick {
        connect(maxContact, miniAddress)
      }
      if (miniAddress.isBlank() or maxContact.isBlank()) disabled()
    }) {
      Text("Connect!")
    }
  }
}

fun reload(miniAddress: String) {
  scope.launch {
    val newCoins = MDS.getCoins(address = miniAddress, sendable = false)
    coins.forEach {
      scene.remove(it.key)
    }
    coins.clear()
    focused = null
    camera.remove(menu)
//          val newCoins = listOf(
//            minima.Coin("", ONE, ONE, "0xC01N1D", true, "0x00"),
//            minima.Coin("", ONE, ONE, "0xC01N1D2", true, "0x01"),
//          )
    newCoins.forEachIndexed { i, coin ->
      val coinObj = Object3D()
      coinObj.name = coin.coinId
      val ball = Mesh(SphereGeometry(3, 100, 100), MeshStandardMaterial().apply{
        map = minimaTex
      })
      coinObj.add(ball)
      val text = Block(coinProps).apply {
        add(Block(BlockProps().apply {
          width = 10.0
          height = 1.0
          backgroundOpacity = 0.0
        }).apply {
          add(Text(TextProps(coin.tokenAmount?.toPlainString() ?: coin.amount.toPlainString())))
        })
      }
      coinObj.add(text)
      coins.put(coinObj, coin)
      scene.add(coinObj)
      getCoinPosition(coin.coinId)?.let {
        coinObj.position.x = it.x
        coinObj.position.y = it.y
        coinObj.position.z = it.z
      } ?: run{
        coinObj.position.x = 8 * i
        coinObj.position.z = -40
      }
      ball.rotation.y = -PI / 2
      text.position.set(4, 4, 0)
      ThreeMeshUI.update()
    }
  }
}

fun updateButtonStates() {
//    console.log("pointermove ${JSON.stringify(mouse)}")
  if (mouse.x != null && mouse.y != null) {
    raycaster.setFromCamera(mouse, camera)
    val intersects = raycaster.intersectObjects(buttons.toTypedArray(), true)
    val intersected = intersects.getOrNull(0) ?.`object`
      ?.let{ findAncestorInList(it, buttons) as Block? }
      ?.apply{ setState("hovered") }
    
    buttons.filter { it != intersected }.forEach { it.setState("idle") }
  }
}

fun createScene() = Scene().apply {
  val texLoader = TextureLoader()
  val starsTex = texLoader.load("tycho_skymap.jpg")
  minimaTex = texLoader.load("minimaAPKIcon.png")
  val stars = Mesh(SphereGeometry(1e9, 30, 30), MeshBasicMaterial().apply {
    map = starsTex
    side = BackSide
  }).apply {
    scale.y = 0.1
  }
  add(stars)
  add(PointLight(0xffffff, 1))
  add(camera.apply {
    console.log("Initial camera position: ${JSON.stringify(camera.position)}")
  })
}

val cameraRotation = Vector2(0, 0)

fun cameraPosition() = Vector3().apply(camera::getWorldPosition)

var touchStartX: Int? = null
var touchStartY: Int? = null
var touchDistance: Double? = null

var mouse = Vector2()

fun animate() {
  camera.rotation.x = cameraRotation.x
  camera.rotation.y = cameraRotation.y
  ThreeMeshUI.update()
  updateButtonStates()
  renderer.render(scene, camera)
  
  window.requestAnimationFrame { animate() }
}

fun updateCoin(obj: Object3D) {
  updateMenu(obj)
  scope.launch {
    updateCoinPosition(obj.name, CoinPosition(obj.position.x.toDouble(), obj.position.y.toDouble(), obj.position.z.toDouble()))
  }
}

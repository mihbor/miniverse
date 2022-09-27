import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import minima.MDS
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

fun main() {
  MDS.init { msg: dynamic ->
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
  
  var miniAddress by mutableStateOf("")
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
      }
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
        connect(maxContact)
      }
    }) {
      Text("Connect!")
    }
  }
}

fun connect(maxiAddress: String) {
  scope.launch {
    val contacts = getContacts()
    val contact = contacts.firstOrNull { it.currentaddress == maxiAddress } ?: run{
      addContact(maxiAddress)
      contacts.first{ it.currentaddress == maxiAddress }
    }
    console.log("contact:", contact)
    val delivered = sendMessage(contact.publickey, "test")
    console.log("delivered:", delivered)
  }
}
fun reload(miniAddress: String) {
  scope.launch {
    val newCoins = getCoins(address = miniAddress, sendable = false)
    coins.forEach {
      scene.remove(it.key)
    }
    coins.clear()
    focused = null
    camera.remove(menu)
//          val newCoins = listOf(
//            Coin("", ONE, ONE, "0xC01N1D", true, "0x00"),
//            Coin("", ONE, ONE, "0xC01N1D2", true, "0x01"),
//          )
    newCoins.forEachIndexed { i, coin ->
      val coinObj = Object3D()
      coinObj.name = coin.coinid
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
          add(Text(TextProps(coin.tokenamount?.toPlainString() ?: coin.amount.toPlainString())))
        })
      }
      coinObj.add(text)
      coins.put(coinObj, coin)
      scene.add(coinObj)
      val sql = MDS.sql("SELECT * FROM coinpos WHERE coinid = '${coin.coinid}';")
      val rows = sql.rows as Array<dynamic>
      console.log("rows:", rows)
      rows.takeIf { it.size == 1 }?.let {
        coinObj.position.x = (it[0].X as String).toDouble()
        coinObj.position.y = (it[0].Y as String).toDouble()
        coinObj.position.z = (it[0].Z as String).toDouble()
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

fun updateButtons() {
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
  updateButtons()
  renderer.render(scene, camera)
  
  window.requestAnimationFrame { animate() }
}

fun updateCoin(obj: Object3D) {
  updateMenu(obj)
  scope.launch {
    MDS.sql("""INSERT INTO coinpos VALUES ('${obj.name}', ${obj.position.x}, ${obj.position.y}, ${obj.position.z})
      ON DUPLICATE KEY UPDATE x = ${obj.position.x}, y = ${obj.position.y}, z = ${obj.position.z};""")
  }
}

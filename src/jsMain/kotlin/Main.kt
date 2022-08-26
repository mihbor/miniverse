import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import minima.MDS
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.TextInput
import org.jetbrains.compose.web.renderComposableInBody
import org.w3c.dom.Touch
import org.w3c.dom.TouchEvent
import org.w3c.dom.Window
import org.w3c.dom.events.Event
import org.w3c.dom.events.WheelEvent
import org.w3c.dom.get
import org.w3c.dom.pointerevents.PointerEvent
import three.js.*
import three.mesh.ui.Block
import three.mesh.ui.BlockProps
import three.mesh.ui.TextProps
import three.mesh.ui.ThreeMeshUI
import kotlin.math.PI
import kotlin.math.sqrt

val scope = MainScope()

val Window.aspectRatio get() = innerWidth.toDouble() / innerHeight

val camera = PerspectiveCamera(60, window.aspectRatio, 0.5, 2e9)

var minimaTex: Texture? = null

operator fun Number.minus(other: Number) = toDouble() - other.toDouble()
operator fun Number.plus(other: Number) = toDouble() + other.toDouble()
operator fun Number.times(other: Number) = toDouble() * other.toDouble()
operator fun Number.div(other: Number) = toDouble() / other.toDouble()
operator fun Number.compareTo(other: Number) = toDouble().compareTo(other.toDouble())

fun square(base: Int) = base * base

val renderer = WebGLRenderer((js("{}") as WebGLRendererParameters).apply{
  antialias = false
  logarithmicDepthBuffer = false
}).apply {
//    document.body?.appendChild( VRButton.createButton(this) )
  document.body?.appendChild(domElement)
  setSize(window.innerWidth-20, window.innerHeight-40)
  setPixelRatio(window.devicePixelRatio)
  xr.enabled = false
}

val coins = mutableStateListOf<Pair<Coin, Object3D>>()
val scene = createScene()

fun main() {
  MDS.init {  }
  window.onresize = {
    camera.aspect = window.aspectRatio
    camera.updateProjectionMatrix()
    
    renderer.setSize(window.innerWidth, window.innerHeight-4)
  }
  createCoordinateDisplay()
  
  registerListeners()
  
  animate()
  
  var miniAddress by mutableStateOf("")
  renderComposableInBody {
    TextInput(miniAddress) {
      style {
        width(200.px)
      }
      onInput {
        miniAddress = it.value
      }
    }
    Button({
      onClick {
        scope.launch {
          val newCoins = coinsForAddress(miniAddress)
          coins.forEach {
            scene.remove(it.second)
          }
          coins.clear()
          newCoins.forEachIndexed { i, coin ->
            val coinObj = Object3D()
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
                add(three.mesh.ui.Text(TextProps(coin.tokenamount?.toPlainString() ?: coin.amount.toPlainString())))
              })
            }
            coinObj.add(text)
            coins.add(coin to coinObj)
            scene.add(coinObj)
            coinObj.position.x = 8 * i
            coinObj.position.z = -40
            ball.rotation.y = -PI / 2
            text.position.set(4, 4, 0)
            ThreeMeshUI.update()
          }
        }
      }
    }) {
      Text("Go!")
    }
  }
}

val uiProps = BlockProps().apply {
  justifyContent = "center"
  alignContent = "center"
  contentDirection = "column"
  padding = 0.02
  borderRadius = 0.05
  fontSize = 0.04
  fontFamily = "fonts/Roboto-msdf.json"
  fontTexture = "fonts/Roboto-msdf.png"
}

val coinProps = BlockProps().apply {
  justifyContent = "center"
  alignContent = "center"
  contentDirection = "column"
  padding = 0.5
  borderRadius = 0.05
  fontSize = 0.5
  fontFamily = "fonts/Roboto-msdf.json"
  fontTexture = "fonts/Roboto-msdf.png"
}

fun createCoordinateDisplay() = Block(uiProps).apply {
  add(Block(BlockProps().apply {
    width = 0.4
    height = 0.3
    backgroundOpacity = 0.0
  }).apply {
    add(three.mesh.ui.Text(TextProps("Test text\n")))
  })
  camera.add(this)
  position.set(1, -0.7, -2)
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

external interface Options {
  var passive: Boolean
}

val cameraRotation = Vector2(0, 0)

fun cameraPosition() = Vector3().apply(camera::getWorldPosition)

var touchStartX: Int? = null
var touchStartY: Int? = null
var touchDistance: Double? = null

var mouse = Vector2()

fun registerListeners() {
  val options = (js("{}") as Options).apply{
    passive = false
  }
  
  document.addEventListener("wheel", ::wheelHandler, options)
//  document.addEventListener("click", ::clickHandler, false)
  document.addEventListener("touchstart", ::touchStartHandler, options)
  document.addEventListener("touchmove", ::touchMoveHandler, options)
  document.addEventListener("pointermove", ::pointerMoveHandler, options)
}

fun wheelHandler(event: Event) {
  if (event is WheelEvent) {
    event.preventDefault()
    cameraRotation.x += event.deltaY / PI / 100
    cameraRotation.y += event.deltaX / PI / 100
  }
}

fun touchStartHandler(event: Event) {
  if (event is TouchEvent) {
    touchStartX = event.touches[0]?.pageX
    touchStartY = event.touches[0]?.pageY
    if (event.touches.length >= 2) {
      touchDistance = distance(event.touches[1]!!, event.touches[0]!!)
    }
  }
}

fun distance(touch1: Touch, touch2: Touch) = sqrt(
  square(touch1.pageX - touch2.pageX).toDouble()
    + square(touch1.pageY - touch2.pageY).toDouble()
)

fun touchMoveHandler(event: Event) {
  if (event is TouchEvent && touchStartX != null) {
    event.preventDefault()
    val touchX = event.touches[0]!!.pageX
    val touchY = event.touches[0]!!.pageY
    val deltaX = touchX - touchStartX!!
    val deltaY = touchY - touchStartY!!
    cameraRotation.x += deltaY / PI / 200
    cameraRotation.y += deltaX / PI / 200
    touchStartX = touchX
    touchStartY = touchY
  }
}

fun pointerMoveHandler(event: Event) {
  if (event is PointerEvent) {
    mouse.x = 2.0 * event.clientX / window.innerWidth - 1
    mouse.y = 1 -2.0 * event.clientY / window.innerHeight
  }
}

fun animate() {
  camera.rotation.x = cameraRotation.x
  camera.rotation.y = cameraRotation.y
  ThreeMeshUI.update()
  
  renderer.render(scene, camera)
  
  window.requestAnimationFrame { animate() }
}
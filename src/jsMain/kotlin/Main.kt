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
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.TextInput
import org.jetbrains.compose.web.renderComposableInBody
import org.w3c.dom.Touch
import org.w3c.dom.TouchEvent
import org.w3c.dom.Window
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.events.WheelEvent
import org.w3c.dom.get
import org.w3c.dom.pointerevents.PointerEvent
import three.js.*
import three.mesh.ui.*
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

val coins = mutableStateMapOf<Coin, Object3D>()
val scene = createScene()

val buttons = mutableListOf<Block>()

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
            scene.remove(it.value)
          }
          coins.clear()
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
            coins.put(coin, coinObj)
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

var menu: Object3D? = null
val xText = Text(TextProps("X: "))
val yText = Text(TextProps("Y: "))
val zText = Text(TextProps("Z: "))
val menuText = Text(TextProps("Selected: ${focused?.name}\n"))

val plusMinusButtonProps = BlockProps().apply {
  width = 0.1
  height = 0.1
  justifyContent = "center"
  alignContent = "center"
  padding = 0.02
  borderRadius = 0.05
}
val hoveredButtonState = BlockState(
  state = "hovered",
  attributes = BlockProps().apply {
    offset = 0.035
    backgroundColor = Color(0x999999)
    backgroundOpacity = 1.0
    fontColor = Color(0xffffff)
  }
)
val idleButtonState = BlockState(
  state = "idle",
  attributes = BlockProps().apply {
    offset = 0.035
    backgroundColor = Color(0x666666)
    backgroundOpacity = 0.3
    fontColor = Color(0xffffff)
  }
)

val xControl = Block(BlockProps().apply {
  contentDirection = "row"
  backgroundOpacity = 0.0
}).apply {
  val minusButton = Block(plusMinusButtonProps).apply {
    add(Text(TextProps("-")))
    setupState(BlockState(
      state = "selected",
      attributes = plusMinusButtonProps,
      onSet = { focused?.let { it.position.x -= 1.0; updateCoin(it); console.log("decX") } }
    ))
    setupState(idleButtonState)
    setupState(hoveredButtonState)
  }
  add(minusButton)
  buttons.add(minusButton)
  add(Block(BlockProps().apply {
    width = 0.2
    height = 0.1
    backgroundOpacity = 0.0
    justifyContent = "center"
    alignContent = "center"
    padding = 0.02
    borderRadius = 0.05
  }).apply {
    add(xText)
  })
  val plusButton = Block(plusMinusButtonProps).apply {
    add(Text(TextProps("+")))
    setupState(BlockState(
      state = "selected",
      attributes = plusMinusButtonProps,
      onSet = { focused?.let { it.position.x += 1.0; updateCoin(it); console.log("incX") } }
    ))
    setupState(idleButtonState)
    setupState(hoveredButtonState)
  }
  add(plusButton)
  buttons.add(plusButton)
}
val yControl = Block(BlockProps().apply {
  contentDirection = "row"
  backgroundOpacity = 0.0
}).apply{
  val minusButton = Block(plusMinusButtonProps).apply {
    add(Text(TextProps("-")))
    setupState(BlockState(
      state = "selected",
      attributes = plusMinusButtonProps,
      onSet = { focused?.let { it.position.y -= 1.0; updateCoin(it); console.log("decY") } }
    ))
    setupState(idleButtonState)
    setupState(hoveredButtonState)
  }
  add(minusButton)
  add(Block(BlockProps().apply {
    width = 0.2
    height = 0.1
    backgroundOpacity = 0.0
    justifyContent = "center"
    alignContent = "center"
    padding = 0.02
    borderRadius = 0.05
  }).apply {
    add(yText)
  })
  val plusButton = Block(plusMinusButtonProps).apply {
    add(Text(TextProps("+")))
    setupState(BlockState(
      state = "selected",
      attributes = plusMinusButtonProps,
      onSet = { focused?.let { it.position.y += 1.0; updateCoin(it); console.log("incY") } }
    ))
    setupState(idleButtonState)
    setupState(hoveredButtonState)
  }
  add(plusButton)
  buttons += minusButton
  buttons += plusButton
}
val zControl = Block(BlockProps().apply {
  contentDirection = "row"
  backgroundOpacity = 0.0
}).apply{
  val minusButton = Block(plusMinusButtonProps).apply {
    add(Text(TextProps("-")))
    setupState(BlockState(
      state = "selected",
      attributes = plusMinusButtonProps,
      onSet = { focused?.let { it.position.z -= 1.0; updateCoin(it); console.log("decZ") } }
    ))
    setupState(idleButtonState)
    setupState(hoveredButtonState)
  }
  add(minusButton)
  buttons += minusButton
  add(Block(BlockProps().apply {
    width = 0.2
    height = 0.1
    backgroundOpacity = 0.0
    justifyContent = "center"
    alignContent = "center"
    padding = 0.02
    borderRadius = 0.05
  }).apply {
    add(zText)
  })
  val plusButton = Block(plusMinusButtonProps).apply {
    add(Text(TextProps("+")))
    setupState(BlockState(
      state = "selected",
      attributes = plusMinusButtonProps,
      onSet = { focused?.let { it.position.z += 1.0; updateCoin(it); console.log("incZ") } }
    ))
    setupState(idleButtonState)
    setupState(hoveredButtonState)
  }
  add(plusButton)
  buttons += plusButton
}

fun createMenuDisplay() = Block(uiProps).apply {
  add(Block(BlockProps().apply{
    width = 0.3
    height = 0.1
    backgroundOpacity = 0.0
  }).apply{add(menuText)})
  add(xControl)
  add(yControl)
  add(zControl)
  camera.add(this)
  position.set(1, -0.7, -2)
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
  val options = (js("{}").unsafeCast<Options>()).apply{
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
  updateButtons()
  renderer.render(scene, camera)
  
  window.requestAnimationFrame { animate() }
}

fun clickHandler(event: Event) {
  if (event is MouseEvent) {
    event.preventDefault()
    val click = Vector2()
    val size = Vector2()
    renderer.getSize(size)
    click.x = 2.0 * event.clientX / window.innerWidth - 1
    click.y = 1 -2.0 * event.clientY / window.innerHeight
    raycaster.setFromCamera(click, camera)
    val intersects = raycaster.intersectObjects(scene.children, true)
    val objects = intersects.map{ it.`object` }
    console.log("intersected", objects)
    buttonClicked(objects) ?: objectClicked(objects)
  }
}

fun objectClicked(intersects: List<Object3D>) =
  intersects.filterIsInstance<Mesh<*, *>>()
    .firstOrNull(coins.values::hasObjectInHierarchy)
    ?.let{ findAncestorInList(it, coins.values) }
    ?.let(::focusOn)

fun focusOn(obj: Object3D) {
  console.log("focused on", obj)
  focused = obj
  updateMenu(obj)
}

fun updateCoin(obj: Object3D) {
  updateMenu(obj)
  scope.launch {
    MDS.sql("""INSERT INTO coinpos VALUES ('${obj.name}', ${obj.position.x}, ${obj.position.y}, ${obj.position.z})
      ON DUPLICATE KEY UPDATE x = ${obj.position.x}, y = ${obj.position.y}, z = ${obj.position.z};""")
  }
}

fun updateMenu(obj: Object3D) {
  val pos = Vector3().let(obj::getWorldPosition)
  menuText.set(TextProps("Selected: ${focused?.name?.take(8)}..."))
  xText.set(TextProps("X: ${pos.x}"))
  yText.set(TextProps("Y: ${pos.y}"))
  zText.set(TextProps("Z: ${pos.z}"))
}

fun buttonClicked(intersects: List<Object3D>): Block? {
//  console.log("Something clicked")
  return intersects.getOrNull(0)
    ?.let { findAncestorInList(it, buttons) as Block? }
    ?.also{ console.log("found button", it)}
    ?.apply { setState("selected") }
}

fun findAncestorInList(child: Object3D, list: Collection<Object3D>): Object3D? =
  if (list.contains(child)) child
  else child.parent?.let { findAncestorInList(it, list)}

fun Collection<Object3D>.hasObjectInHierarchy(obj: Object3D): Boolean =
  if (contains(obj)) true
  else obj.parent != null && this.hasObjectInHierarchy(obj.parent!!)

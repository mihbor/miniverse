import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.launch
import minima.Coin
import org.w3c.dom.Touch
import org.w3c.dom.TouchEvent
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.events.WheelEvent
import org.w3c.dom.get
import org.w3c.dom.pointerevents.PointerEvent
import three.js.*
import three.mesh.ui.*
import kotlin.math.PI
import kotlin.math.sqrt

val buttons = mutableListOf<Block>()
lateinit var menu: Object3D
val xText = Text(TextProps("X: "))
val yText = Text(TextProps("Y: "))
val zText = Text(TextProps("Z: "))
val menuText = Text(TextProps("Selected: ${focused?.name}\n"))

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

val plusMinusButtonProps = BlockProps().apply {
  width = 0.1
  height = 0.1
  justifyContent = "center"
  alignContent = "center"
  padding = 0.02
  borderRadius = 0.05
}

val plusMinusTextProps = BlockProps().apply {
  width = 0.2
  height = 0.1
  backgroundOpacity = 0.0
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
  add(Block(plusMinusTextProps).apply {
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
  add(Block(plusMinusTextProps).apply {
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
  add(Block(plusMinusTextProps).apply {
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
  position.set(0, -0.7, -2)
}

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
    .firstOrNull(coins.keys::hasObjectInHierarchy)
    ?.let { findAncestorInMap(it, coins) }
    ?.let { (obj, coin) -> focusOn(obj, coin) }

fun focusOn(obj: Object3D, coin: Coin) {
  console.log("focused on", obj)
  focused = obj
  scope.launch {
    if (getCoins(coinId = coin.coinid, sendable = true).isNotEmpty()) {
      console.log("sendable: true")
      if (!camera.children.contains(menu)) {
        camera.add(menu)
        updateMenu(obj)
      }
    } else {
      camera.remove(menu)
    }
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

fun findAncestorInMap(child: Object3D, map: Map<Object3D, Coin>): Pair<Object3D, Coin>? =
  map[child]?.let { child to it }
    ?: child.parent?.let { findAncestorInMap(it, map)}

fun Collection<Object3D>.hasObjectInHierarchy(obj: Object3D): Boolean =
  if (contains(obj)) true
  else obj.parent != null && this.hasObjectInHierarchy(obj.parent!!)

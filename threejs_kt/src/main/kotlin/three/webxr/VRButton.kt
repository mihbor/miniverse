@file:JsModule("three/examples/jsm/webxr/VRButton")
@file:JsNonModule
package three.webxr

import org.w3c.dom.Node
import three.js.Renderer

external object VRButton {
    fun createButton(renderer: Renderer): Node
}

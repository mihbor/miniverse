@file:JsModule("three-mesh-ui")
@file:JsNonModule
package three.mesh.ui

import three.js.Color
import three.js.Object3D

@JsName("default")
external object ThreeMeshUI {
    fun update()
}

external interface BlockProps {
    var width: Double
    var height: Double
    var padding: Double
    var offset: Double
    var margin: Double
    var fontSize: Double
    var borderRadius: Double
    var backgroundOpacity: Double
    var justifyContent: String
    var alignContent: String
    var fontFamily: String
    var fontTexture: String
    var contentDirection: String
    var backgroundColor: Color
    var fontColor: Color
}

external interface BlockState {
    var state: String
    var attributes: BlockProps
    var onSet: () -> Unit
}

external class Block(options: BlockProps) : Object3D {
    fun setupState(state: BlockState)
    fun setState(state: String)
}

external interface TextProps {
    var content: String
}
external class Text(options: TextProps) : Object3D {
    fun set(options: TextProps)
}

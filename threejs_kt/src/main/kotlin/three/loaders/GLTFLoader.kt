@file:JsModule("three/examples/jsm/loaders/GLTFLoader")
@file:JsNonModule

package three.js.loaders

import org.w3c.dom.ErrorEvent
import org.w3c.xhr.ProgressEvent
import three.js.Loader
import three.js.Object3D

external interface GLTF {
    var scene: Object3D
}

@JsName("GLTFLoader")
external class GLTFLoader : Loader {
    fun load(
        url: String,
        onLoad: (GLTF) -> Unit = definedExternally,
        onProgress: (ProgressEvent) -> Unit = definedExternally,
        onError: (ErrorEvent) -> Unit = definedExternally
    )
}
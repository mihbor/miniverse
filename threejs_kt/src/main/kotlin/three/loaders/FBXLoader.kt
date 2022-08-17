@file:JsModule("three/examples/jsm/loaders/FBXLoader")
@file:JsNonModule
package three.js.loaders

import org.w3c.dom.ErrorEvent
import org.w3c.xhr.ProgressEvent
import three.js.Loader

@JsName("FBXLoader")
external class FBXLoader : Loader {
    fun load(
        url: String,
        onLoad: (GLTF) -> Unit = definedExternally,
        onProgress: (ProgressEvent) -> Unit = definedExternally,
        onError: (ErrorEvent) -> Unit = definedExternally
    )
}
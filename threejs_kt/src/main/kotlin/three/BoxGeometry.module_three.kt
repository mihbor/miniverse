@file:JsModule("three")
@file:JsNonModule
@file:Suppress("ABSTRACT_MEMBER_NOT_IMPLEMENTED", "VAR_TYPE_MISMATCH_ON_OVERRIDE", "INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS", "EXTERNAL_DELEGATION", "PackageDirectoryMismatch")
package three.js

import kotlin.js.*
import kotlin.js.Json
import org.khronos.webgl.*
import org.w3c.dom.*
import org.w3c.dom.events.*
import org.w3c.dom.parsing.*
import org.w3c.dom.svg.*
import org.w3c.dom.url.*
import org.w3c.fetch.*
import org.w3c.files.*
import org.w3c.notifications.*
import org.w3c.performance.*
import org.w3c.workers.*
import org.w3c.xhr.*

external interface `T$41` {
    var width: Number
    var height: Number
    var depth: Number
    var widthSegments: Number
    var heightSegments: Number
    var depthSegments: Number
}

open external class BoxGeometry(width: Number = definedExternally, height: Number = definedExternally, depth: Number = definedExternally, widthSegments: Number = definedExternally, heightSegments: Number = definedExternally, depthSegments: Number = definedExternally) : Geometry {
    override var type: String
    open var parameters: `T$41`
}
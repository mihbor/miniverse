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

external interface PointsMaterialParameters : MaterialParameters {
    var color: dynamic /* Color? | String? | Number? */
        get() = definedExternally
        set(value) = definedExternally
    var map: Texture?
        get() = definedExternally
        set(value) = definedExternally
    var alphaMap: Texture?
        get() = definedExternally
        set(value) = definedExternally
    var size: Number?
        get() = definedExternally
        set(value) = definedExternally
    var sizeAttenuation: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var morphTargets: Boolean?
        get() = definedExternally
        set(value) = definedExternally
}

open external class PointsMaterial(parameters: PointsMaterialParameters = definedExternally) : Material {
    override var type: String
    open var color: Color
    open var map: Texture?
    open var alphaMap: Texture?
    open var size: Number
    open var sizeAttenuation: Boolean
    open var morphTargets: Boolean
    open fun setValues(parameters: PointsMaterialParameters)
    override fun setValues(values: MaterialParameters)
}
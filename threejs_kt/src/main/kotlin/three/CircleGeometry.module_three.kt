@file:JsModule("three")
@file:JsNonModule
@file:Suppress("ABSTRACT_MEMBER_NOT_IMPLEMENTED", "VAR_TYPE_MISMATCH_ON_OVERRIDE", "INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS", "EXTERNAL_DELEGATION", "PackageDirectoryMismatch")
package three.js

external interface `T$40` {
    var radius: Number
    var segments: Number
    var thetaStart: Number
    var thetaLength: Number
}

open external class CircleGeometry(radius: Number = definedExternally, segments: Number = definedExternally, thetaStart: Number = definedExternally, thetaLength: Number = definedExternally) : Geometry {
    override var type: String
    open var parameters: `T$40`
}
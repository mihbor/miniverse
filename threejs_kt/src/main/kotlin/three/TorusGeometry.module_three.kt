@file:JsModule("three")
@file:JsNonModule
@file:Suppress("ABSTRACT_MEMBER_NOT_IMPLEMENTED", "VAR_TYPE_MISMATCH_ON_OVERRIDE", "INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS", "EXTERNAL_DELEGATION", "PackageDirectoryMismatch")
package three.js

external interface `T$31` {
    var radius: Number
    var tube: Number
    var radialSegments: Number
    var tubularSegments: Number
    var arc: Number
}

open external class TorusGeometry(radius: Number = definedExternally, tube: Number = definedExternally, radialSegments: Number = definedExternally, tubularSegments: Number = definedExternally, arc: Number = definedExternally) : Geometry {
    override var type: String
    open var parameters: `T$31`
}
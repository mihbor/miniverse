@file:JsModule("three")
@file:JsNonModule
@file:Suppress("ABSTRACT_MEMBER_NOT_IMPLEMENTED", "VAR_TYPE_MISMATCH_ON_OVERRIDE", "INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS", "EXTERNAL_DELEGATION", "PackageDirectoryMismatch")
package three.js

open external class Clock(autoStart: Boolean = definedExternally) {
    open var autoStart: Boolean
    open var startTime: Number
    open var oldTime: Number
    open var elapsedTime: Number
    open var running: Boolean
    open fun start()
    open fun stop()
    open fun getElapsedTime(): Number
    open fun getDelta(): Number
}
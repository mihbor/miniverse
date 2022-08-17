@file:JsModule("three")
@file:JsNonModule
@file:Suppress("ABSTRACT_MEMBER_NOT_IMPLEMENTED", "VAR_TYPE_MISMATCH_ON_OVERRIDE", "INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS", "EXTERNAL_DELEGATION", "PackageDirectoryMismatch")
package three.js

open external class CurvePath<T : Vector> : Curve<T> {
    override var type: String
    open var curves: Array<Curve<T>>
    open var autoClose: Boolean
    open fun add(curve: Curve<T>)
    open fun closePath()
    open fun getPoint(t: Number): T
    open fun getCurveLengths(): Array<Number>
    open fun createPointsGeometry(divisions: Number): Geometry
    open fun createSpacedPointsGeometry(divisions: Number): Geometry
    open fun createGeometry(points: Array<T>): Geometry
}
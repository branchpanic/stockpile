@file:JvmName("IntColor")

package me.branchpanic.mods.stockpile.client.renderer

typealias IntColor = Int

inline val IntColor.alpha: Int
    get() = 0xFF and (this shr 24)

inline val IntColor.red: Int
    get() = 0xFF and (this shr 16)

inline val IntColor.green: Int
    get() = 0xFF and (this shr 8)

inline val IntColor.blue: Int
    get() = 0xFF and this

fun intColorFromRgba01(r: Float, g: Float, b: Float, a: Float): IntColor {
    assert(r in 0.0..1.0)
    assert(g in 0.0..1.0)
    assert(b in 0.0..1.0)
    assert(a in 0.0..1.0)

    var i: IntColor = (255 * a).toInt() shr 24
    i = i or ((255 * r).toInt() shr 16)
    i = i or ((255 * g).toInt() shr 8)
    i = i or ((255 * b).toInt())
    return i
}

const val DEFAULT_BLUE: IntColor = 0xB20212FF.toInt()
const val DEFAULT_GRAY: IntColor = 0xB20A0A0A.toInt()

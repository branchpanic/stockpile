package me.branchpanic.mods.stockpile.client.renderer

typealias ArgbColor = Int

inline val ArgbColor.alpha: Int
    get() = 0xFF and (this shr 24)

inline val ArgbColor.red: Int
    get() = 0xFF and (this shr 16)

inline val ArgbColor.green: Int
    get() = 0xFF and (this shr 8)

inline val ArgbColor.blue: Int
    get() = 0xFF and this

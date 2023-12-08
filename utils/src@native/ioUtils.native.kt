package io.sellmair.pacemaker.utils

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toCValues
import okio.FileSystem
import platform.Foundation.NSData
import platform.Foundation.create

actual fun defaultFileSystem(): FileSystem {
    return FileSystem.SYSTEM
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
fun ByteArray.toNSData(): NSData {
    return memScoped {
        NSData.create(bytes = this@toNSData.toCValues().ptr, length = size.toULong())
    }
}
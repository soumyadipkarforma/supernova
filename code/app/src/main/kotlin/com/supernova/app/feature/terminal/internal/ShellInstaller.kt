package com.supernova.app.feature.terminal.internal

import android.content.Context
import android.system.Os
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object ShellInstaller {
    fun install(context: Context): File {
        val binDir = File(context.filesDir, "bin")
        if (!binDir.exists()) binDir.mkdirs()

        val busybox = File(binDir, "busybox")
        
        // Always extract to ensure we have the latest version or if it's missing
        try {
            context.assets.open("bin/busybox").use { input ->
                FileOutputStream(busybox).use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: IOException) {
            // If asset is missing (e.g. before user provides it), we can't do much.
            // In production, this would be a fatal error or fallback.
            // For now, we log/swallow to allow app to run if binary isn't there yet.
            e.printStackTrace()
            return binDir
        }

        // Sets executable permission
        busybox.setExecutable(true)

        // Creates symlinks: sh, ls, cp, mv, rm, mkdir, chmod, cat, echo
        val applets = listOf("sh", "ls", "cp", "mv", "rm", "mkdir", "chmod", "cat", "echo")
        applets.forEach { applet ->
            val link = File(binDir, applet)
            // Re-create link to be safe
            if (link.exists()) link.delete()
            try {
                Os.symlink(busybox.absolutePath, link.absolutePath)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        return binDir
    }
}

package com.supernova.app.core.shell

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

object BootstrapManager {
    private const val BOOTSTRAP_FILE = "bootstrap.zip"
    private const val MARKER_FILE = ".bootstrap_installed"

    fun isInstalled(context: Context): Boolean {
        return File(context.filesDir, MARKER_FILE).exists()
    }

    fun install(context: Context) {
        val installDir = context.filesDir
        val marker = File(installDir, MARKER_FILE)

        if (marker.exists()) return

        try {
            // Check if bootstrap.zip exists in assets
            val assetManager = context.assets
            val assets = assetManager.list("")
            if (assets == null || !assets.contains(BOOTSTRAP_FILE)) {
                // If no bootstrap found, we might be in a dev mode or it's missing.
                // For this standalone architecture demonstration, we will create 
                // a dummy structure if the real zip isn't present, or throw execution error.
                // In a real build, bootstrap.zip MUST be in assets.
                return
            }

            // Extract Zip
            assetManager.open(BOOTSTRAP_FILE).use { inputStream ->
                ZipInputStream(inputStream).use { zipStream ->
                    var entry = zipStream.nextEntry
                    while (entry != null) {
                        val outFile = File(installDir, entry.name)
                        if (entry.isDirectory) {
                            outFile.mkdirs()
                        } else {
                            outFile.parentFile?.mkdirs()
                            FileOutputStream(outFile).use { output ->
                                zipStream.copyTo(output)
                            }
                            // Set executable permission for binaries
                            if (outFile.parent?.endsWith("bin") == true || outFile.name == "sh" || outFile.name == "busybox") {
                                outFile.setExecutable(true)
                            }
                        }
                        entry = zipStream.nextEntry
                    }
                }
            }

            // Create marker
            marker.createNewFile()
            
        } catch (e: Exception) {
            e.printStackTrace()
            // In a real app, report installation failure
        }
    }
    
    fun getBinDir(context: Context): File {
        return File(context.filesDir, "usr/bin")
    }
}

package com.supernova.app.core.fs

import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow

object FileManager {
    val root = File("/storage/emulated/0/workspace")

    val fileSystemTick = MutableStateFlow(0L)

    fun ensureWorkspace() {
        if (!root.exists()) {
            root.mkdirs()
        }
    }

    fun listFiles(dir: File = root): List<File> {
        return dir.listFiles()?.sortedWith(compareBy({ !it.isDirectory }, { it.name })) ?: emptyList()
    }

    fun readFile(file: File): String {
        return try {
            file.readText()
        } catch (e: Exception) {
            ""
        }
    }

    fun saveFile(file: File, content: String) {
        try {
            if (!file.parentFile.exists()) file.parentFile.mkdirs()
            file.writeText(content)
            fileSystemTick.value = System.currentTimeMillis()
        } catch (e: Exception) {
            // Handle error
        }
    }
    
    fun deleteFile(file: File) {
        if (file.isDirectory) file.deleteRecursively() else file.delete()
        fileSystemTick.value = System.currentTimeMillis()
    }
}

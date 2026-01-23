package com.supernova.app.core.fs

import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow

object FileManager {
    val root = File("/storage/emulated/0/workspace")
    var currentDirectory = root

    val fileSystemTick = MutableStateFlow(0L)

    fun ensureWorkspace() {
        if (!root.exists()) {
            root.mkdirs()
        }
    }

    fun listFiles(dir: File = currentDirectory): List<File> {
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

    fun refreshFileSystem() {
        fileSystemTick.value = System.currentTimeMillis()
    }

    fun navigateTo(directory: File) {
        if (directory.exists() && directory.isDirectory) {
            currentDirectory = directory
            refreshFileSystem()
        }
    }

    fun navigateUp() {
        val parent = currentDirectory.parentFile
        if (parent != null) {
            currentDirectory = parent
            refreshFileSystem()
        }
    }

    fun compressToZip(source: File, destination: File) {
        try {
            java.util.zip.ZipOutputStream(java.io.FileOutputStream(destination)).use { zipOut ->
                if (source.isDirectory) {
                    source.walkTopDown().forEach { file ->
                        if (file != source) {  // Skip the root directory itself
                            val entryName = source.toURI().relativize(file.toURI()).path
                            val entry = java.util.zip.ZipEntry(if (file.isDirectory) "$entryName/" else entryName)
                            zipOut.putNextEntry(entry)

                            if (file.isFile) {
                                file.inputStream().use { it.copyTo(zipOut) }
                            }

                            zipOut.closeEntry()
                        }
                    }
                } else {
                    val entry = java.util.zip.ZipEntry(source.name)
                    zipOut.putNextEntry(entry)
                    source.inputStream().use { it.copyTo(zipOut) }
                    zipOut.closeEntry()
                }
            }
            refreshFileSystem()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

package com.supernova.app.feature.terminal.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class ShellSession(
    private val workingDir: File,
    private val scope: CoroutineScope
) {
    private var process: Process? = null
    private var writer: OutputStreamWriter? = null
    
    private val _output = MutableSharedFlow<String>()
    val output: SharedFlow<String> = _output

    fun start() {
        if (process != null) return

        scope.launch(Dispatchers.IO) {
            try {
                process = ProcessBuilder("/system/bin/sh")
                    .directory(workingDir)
                    .redirectErrorStream(true)
                    .start()

                writer = OutputStreamWriter(process!!.outputStream)
                val reader = BufferedReader(InputStreamReader(process!!.inputStream))

                // Initial prompt or info
                _output.emit("Supernova Internal Shell\n")
                _output.emit("Working Directory: ${workingDir.absolutePath}\n")
                _output.emit("$ ")

                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    _output.emit(line + "\n")
                }
            } catch (e: Exception) {
                _output.emit("Error starting shell: ${e.message}\n")
            }
        }
    }

    fun sendCommand(command: String) {
        scope.launch(Dispatchers.IO) {
            try {
                writer?.write(command + "\n")
                writer?.flush()
            } catch (e: Exception) {
                _output.emit("Error sending command: ${e.message}\n")
            }
        }
    }

    fun close() {
        try {
            process?.destroy()
        } catch (e: Exception) {
            // Ignore
        }
    }
}

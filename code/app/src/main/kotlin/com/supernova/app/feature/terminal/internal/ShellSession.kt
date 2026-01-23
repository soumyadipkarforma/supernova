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
    private val binDir: File,
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
                // Uses ProcessBuilder
                // Executes: sh
                val pb = ProcessBuilder("sh")
                
                // Sets working directory to: /storage/emulated/0/workspace (passed as workingDir)
                pb.directory(workingDir)
                pb.redirectErrorStream(true)
                
                // Sets PATH to include internal bin dir
                val env = pb.environment()
                val currentPath = env["PATH"] ?: ""
                env["PATH"] = "${binDir.absolutePath}:${currentPath}"
                
                process = pb.start()

                writer = OutputStreamWriter(process!!.outputStream)
                val reader = BufferedReader(InputStreamReader(process!!.inputStream))

                // Initial banner
                _output.emit("SuperNova Embedded Shell (BusyBox)\n")
                _output.emit("Home: ${workingDir.absolutePath}\n")
                _output.emit("$ ")

                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    _output.emit(line + "\n")
                }
                
                val exitCode = process?.waitFor()
                _output.emit("\n[Process exited with code $exitCode]\n")

            } catch (e: Exception) {
                _output.emit("Error starting shell: ${e.message}\n")
                // Fallback debug info
                _output.emit("Checked bin dir: ${binDir.absolutePath}\n")
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

    fun sendRaw(text: String) {
        scope.launch(Dispatchers.IO) {
            try {
                writer?.write(text)
                writer?.flush()
            } catch (e: Exception) {
                _output.emit("Error sending raw input: ${e.message}\n")
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
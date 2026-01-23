package com.supernova.app.core.shell

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.io.File
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class ShellSession(
    private val workingDir: File,
    private val onProcessExit: (Int) -> Unit
) {
    private var process: Process? = null
    private var inputWriter: OutputStreamWriter? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _outputFlow = MutableSharedFlow<String>(replay = 0)
    val outputFlow = _outputFlow.asSharedFlow()

    fun start() {
        if (!workingDir.exists()) workingDir.mkdirs()

        val pb = ProcessBuilder("/system/bin/sh")
        pb.directory(workingDir)
        pb.redirectErrorStream(true)

        val termuxPath = "/data/data/com.termux/files"
        val termuxBin = "$termuxPath/usr/bin"
        val env = pb.environment()
        
        // Inject Termux Binaries into PATH
        env["PATH"] = "$termuxBin:${env["PATH"]}"
        env["LD_LIBRARY_PATH"] = "$termuxPath/usr/lib"
        env["HOME"] = workingDir.absolutePath
        env["TERM"] = "xterm-256color"

        try {
            process = pb.start()
            inputWriter = OutputStreamWriter(process!!.outputStream)

            scope.launch {
                val reader = BufferedReader(InputStreamReader(process!!.inputStream))
                try {
                    var line: String? = reader.readLine()
                    while (line != null) {
                        _outputFlow.emit(line + "\n")
                        line = reader.readLine()
                    }
                } catch (e: Exception) {
                    _outputFlow.emit("[Process Error]: ${e.message}\n")
                } finally {
                    val exitCode = process?.waitFor() ?: -1
                    onProcessExit(exitCode)
                }
            }
            
            // Auto-init bash if available in Termux
            sendCommand("export PATH=$termuxBin:\$PATH")
            sendCommand("if [ -f $termuxBin/bash ]; then exec $termuxBin/bash; fi")

        } catch (e: Exception) {
            scope.launch { _outputFlow.emit("[System Error]: Could not spawn process. ${e.message}\n") }
        }
    }

    fun sendCommand(command: String) {
        scope.launch {
            try {
                inputWriter?.write(command + "\n")
                inputWriter?.flush()
            } catch (e: Exception) {
                _outputFlow.emit("[Input Error]: Pipe broken.\n")
            }
        }
    }

    fun destroy() {
        process?.destroy()
        scope.cancel()
    }
}

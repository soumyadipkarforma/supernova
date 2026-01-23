package com.supernova.app.core.shell

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.io.File
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class ShellSession(
    private val context: android.content.Context,
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

        // Determine internal paths
        val usrDir = File(context.filesDir, "usr")
        val binDir = File(usrDir, "bin")
        val libDir = File(usrDir, "lib")
        val shellPath = File(binDir, "sh").absolutePath
        
        // Fallback to system shell if bootstrap isn't ready (or for initial debug)
        val executable = if (File(shellPath).exists()) shellPath else "/system/bin/sh"

        val pb = ProcessBuilder(executable)
        pb.directory(workingDir)
        pb.redirectErrorStream(true)

        val env = pb.environment()
        
        // Inject Standalone Environment
        env["PATH"] = "${binDir.absolutePath}:${env["PATH"]}"
        env["LD_LIBRARY_PATH"] = libDir.absolutePath
        env["HOME"] = workingDir.absolutePath
        env["TERM"] = "xterm-256color"
        env["PREFIX"] = usrDir.absolutePath

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
            
            // Auto-init bash if available
            sendCommand("export PATH=${binDir.absolutePath}:\$PATH")
            sendCommand("if [ -f ${binDir.absolutePath}/bash ]; then exec ${binDir.absolutePath}/bash; fi")

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

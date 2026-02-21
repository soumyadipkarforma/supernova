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
        
        // Use bash if available, else sh
        val bashPath = File(binDir, "bash").absolutePath
        val executable = if (File(bashPath).exists()) bashPath else if (File(shellPath).exists()) shellPath else "/system/bin/sh"

        // For login shell behavior, we prepend a hyphen to the process name or use --login
        // In ProcessBuilder, we can't easily change the process name (argv[0]), 
        // so we use the --login flag if bash is used.
        val pb = if (executable.endsWith("bash")) {
            ProcessBuilder(executable, "--login")
        } else {
            ProcessBuilder(executable)
        }
        
        pb.directory(workingDir)
        pb.redirectErrorStream(true)

        val env = pb.environment()
        
        // Exact Termux-like Environment
        env["PATH"] = "${binDir.absolutePath}:${env["PATH"]}"
        env["LD_LIBRARY_PATH"] = libDir.absolutePath
        env["HOME"] = "/storage/emulated/0/sworkspace"
        env["TERM"] = "xterm-256color"
        env["PREFIX"] = usrDir.absolutePath
        env["TMPDIR"] = File(usrDir, "tmp").absolutePath
        env["LANG"] = "en_US.UTF-8"

        try {
            process = pb.start()
            inputWriter = OutputStreamWriter(process!!.outputStream)

            scope.launch {
                val reader = BufferedReader(InputStreamReader(process!!.inputStream))
                try {
                    val buffer = CharArray(1024)
                    var charsRead = reader.read(buffer)
                    while (charsRead != -1) {
                        _outputFlow.emit(String(buffer, 0, charsRead))
                        charsRead = reader.read(buffer)
                    }
                } catch (e: Exception) {
                    _outputFlow.emit("[Process Error]: ${e.message}\n")
                } finally {
                    val exitCode = process?.waitFor() ?: -1
                    onProcessExit(exitCode)
                }
            }

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

    fun sendRaw(data: String) {
        scope.launch {
            try {
                inputWriter?.write(data)
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
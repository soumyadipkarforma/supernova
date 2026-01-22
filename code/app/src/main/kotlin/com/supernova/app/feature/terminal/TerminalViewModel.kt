package com.supernova.app.feature.terminal

import androidx.lifecycle.ViewModel
import com.termux.terminal.TerminalSession
import com.termux.terminal.TerminalSessionClient
import java.io.File

class TerminalViewModel : ViewModel() {
    var session: TerminalSession? = null
        private set

    fun initSession(workingDir: File, client: TerminalSessionClient) {
        if (session != null) return

        val shellPath = if (File("/data/data/com.termux/files/usr/bin/bash").exists()) {
            "/data/data/com.termux/files/usr/bin/bash"
        } else {
            "/system/bin/sh"
        }
        
        val env = arrayOf(
            "TERM=xterm-256color",
            "PATH=/data/data/com.termux/files/usr/bin:/data/data/com.termux/files/usr/bin/applets:/system/bin:/system/xbin",
            "HOME=${workingDir.absolutePath}",
            "PREFIX=/data/data/com.termux/files/usr",
            "TMPDIR=/data/data/com.termux/files/usr/tmp"
        )
        
        session = TerminalSession(
            shellPath,
            workingDir.absolutePath,
            arrayOf("-l"), // login shell
            env,
            10000, // transcript rows
            client
        )
    }

    override fun onCleared() {
        super.onCleared()
        session?.finishIfRunning()
    }
}
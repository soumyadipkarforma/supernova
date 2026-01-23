package com.supernova.app.core.shell

import java.io.File

object ProcessManager {
    private val sessions = mutableMapOf<String, ShellSession>()
    val workspaceRoot = File("/storage/emulated/0/workspace")

    fun createSession(context: android.content.Context, id: String, subDir: String = ""): ShellSession {
        val workingDir = if (subDir.isEmpty()) workspaceRoot else File(workspaceRoot, subDir)
        val session = ShellSession(context, workingDir) { exitCode ->
            sessions.remove(id)
        }
        sessions[id] = session
        session.start()
        return session
    }

    fun getSession(id: String): ShellSession? = sessions[id]
    
    fun killSession(id: String) {
        sessions[id]?.destroy()
        sessions.remove(id)
    }

    fun killAll() {
        sessions.values.forEach { it.destroy() }
        sessions.clear()
    }
}

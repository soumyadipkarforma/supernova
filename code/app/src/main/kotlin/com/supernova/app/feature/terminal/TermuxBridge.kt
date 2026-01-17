package com.supernova.app.feature.terminal

import android.content.Context
import android.content.Intent
import android.widget.Toast

object TermuxBridge {
    /**
     * Executes a command in Termux utilizing RUN_COMMAND intent.
     * Ensure "Allow external apps to run commands" is enabled in Termux settings.
     */
    fun runCommand(context: Context, command: String, workingDir: String) {
        try {
            val intent = Intent().apply {
                action = "com.termux.RUN_COMMAND"
                setClassName("com.termux", "com.termux.app.RunCommandService")
                putExtra("com.termux.RUN_COMMAND_PATH", "/data/data/com.termux/files/usr/bin/bash")
                putExtra("com.termux.RUN_COMMAND_ARGUMENTS", arrayOf("-c", command))
                putExtra("com.termux.RUN_COMMAND_WORKDIR", workingDir)
                putExtra("com.termux.RUN_COMMAND_BACKGROUND", false) 
                putExtra("com.termux.RUN_COMMAND_SESSION_ACTION", "0")
            }
            context.startService(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to call Termux: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}

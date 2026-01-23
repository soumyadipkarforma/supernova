package com.supernova.app.feature.terminal

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.supernova.app.feature.terminal.internal.ShellInstaller
import com.supernova.app.feature.terminal.internal.ShellSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.File

class TerminalViewModel(application: Application) : AndroidViewModel(application) {
    private var session: ShellSession? = null
    
    private val _terminalOutput = MutableStateFlow("")
    val terminalOutput: StateFlow<String> = _terminalOutput
    
    fun initSession(workingDir: File) {
        if (session != null) return

        // Install BusyBox tools
        val binDir = ShellInstaller.install(getApplication())
        
        session = ShellSession(workingDir, binDir, viewModelScope)
        session?.start()
        
        viewModelScope.launch {
            session?.output?.collect { newOutput ->
                val current = _terminalOutput.value
                val next = if (current.length > 10000) {
                     current.takeLast(9000) + newOutput
                } else {
                    current + newOutput
                }
                _terminalOutput.value = next
            }
        }
    }
    
    fun sendCommand(cmd: String) {
        session?.sendCommand(cmd)
    }

    fun sendRaw(text: String) {
        session?.sendRaw(text)
    }
    
    override fun onCleared() {
        super.onCleared()
        session?.close()
    }
}

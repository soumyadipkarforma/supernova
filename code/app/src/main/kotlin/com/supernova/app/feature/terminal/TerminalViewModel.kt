package com.supernova.app.feature.terminal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.supernova.app.feature.terminal.internal.ShellSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.File

class TerminalViewModel : ViewModel() {
    private var session: ShellSession? = null
    
    private val _terminalOutput = MutableStateFlow("")
    val terminalOutput: StateFlow<String> = _terminalOutput
    
    // We keep a history of commands if needed, but for now just output
    
    fun initSession(workingDir: File) {
        if (session != null) return
        
        session = ShellSession(workingDir, viewModelScope)
        session?.start()
        
        viewModelScope.launch {
            session?.output?.collect { newOutput ->
                // Append new output (limit size if necessary)
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
    
    override fun onCleared() {
        super.onCleared()
        session?.close()
    }
}

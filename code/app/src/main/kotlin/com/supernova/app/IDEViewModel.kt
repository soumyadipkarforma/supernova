package com.supernova.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.supernova.app.core.fs.FileManager
import com.supernova.app.core.shell.ProcessManager
import com.supernova.app.core.shell.ShellSession
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

class IDEViewModel(application: android.app.Application) : androidx.lifecycle.AndroidViewModel(application) {
    private val _terminalOutput = MutableStateFlow<List<String>>(emptyList())
    val terminalOutput = _terminalOutput.asStateFlow()
    
    private val activeSessionId = "main_terminal"
    private var currentSession: ShellSession? = null

    val activeFileContent = MutableStateFlow("")
    val currentFile = MutableStateFlow<File?>(null)

    val workspaceFiles = FileManager.fileSystemTick.map {
        FileManager.listFiles()
    }.stateIn(viewModelScope, SharingStarted.Lazily, FileManager.listFiles())

    init {
        FileManager.ensureWorkspace()
        startTerminal()
    }

    private fun startTerminal() {
        currentSession = ProcessManager.createSession(getApplication(), activeSessionId)
        
        viewModelScope.launch {
            currentSession?.outputFlow?.collect { newLine ->
                _terminalOutput.update { 
                    val newContent = it.toMutableList()
                    if (newContent.size > 500) newContent.removeAt(0)
                    newContent.add(newLine)
                    newContent
                }
            }
        }
    }

    fun sendTerminalCommand(cmd: String) {
        currentSession?.sendCommand(cmd)
    }

    fun openFile(file: File) {
        currentFile.value = file
        activeFileContent.value = FileManager.readFile(file)
    }

    fun saveFile() {
        currentFile.value?.let { file ->
            FileManager.saveFile(file, activeFileContent.value)
        }
    }

    override fun onCleared() {
        super.onCleared()
        ProcessManager.killAll()
    }
}

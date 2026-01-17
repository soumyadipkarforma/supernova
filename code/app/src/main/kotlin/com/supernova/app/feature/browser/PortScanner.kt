package com.supernova.app.feature.browser

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

object PortScanner {
    suspend fun scanLocalPorts(): List<Int> = withContext(Dispatchers.IO) {
        val activePorts = mutableListOf<Int>()
        val portsToScan = listOf(3000, 5000, 5173, 8000, 8080)
        for (port in portsToScan) {
            try {
                Socket().use { socket ->
                    socket.connect(InetSocketAddress("127.0.0.1", port), 80)
                    activePorts.add(port)
                }
            } catch (e: Exception) {
                // Connection failed = port closed
            }
        }
        activePorts
    }
}

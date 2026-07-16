package com.brunno.appkmp.presentation.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Network.nw_path_get_status
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_status_satisfied
import platform.darwin.dispatch_queue_create

class IOSNetworkMonitor : NetworkMonitor {

    private val _isOffline = MutableStateFlow(false)
    override val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    private val monitor = nw_path_monitor_create()
    private val queue = dispatch_queue_create("NetworkMonitorQueue", null)

    init {
        nw_path_monitor_set_update_handler(monitor) { path ->
            // Se o status for "satisfied", temos internet. Caso contrário, estamos offline.
            val status = nw_path_get_status(path)
            val isConnected = status == nw_path_status_satisfied

            _isOffline.value = !isConnected
        }

        nw_path_monitor_set_queue(monitor, queue)
        nw_path_monitor_start(monitor)
    }
}
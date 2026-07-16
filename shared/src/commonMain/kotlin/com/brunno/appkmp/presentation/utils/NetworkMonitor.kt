package com.brunno.appkmp.presentation.utils

import kotlinx.coroutines.flow.StateFlow

interface NetworkMonitor {
    val isOffline: StateFlow<Boolean>
}
package com.brunno.appkmp.presentation.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class GlobalErrorHandler {
    private val _fatalError = MutableStateFlow<Throwable?>(null)
    val fatalError = _fatalError.asStateFlow()

    fun triggerError(error: Throwable) {
        _fatalError.value = error
    }

    fun clearError() {
        _fatalError.value = null
    }
}
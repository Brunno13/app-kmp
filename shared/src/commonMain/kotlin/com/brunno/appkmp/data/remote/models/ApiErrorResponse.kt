package com.brunno.appkmp.data.remote.models

import kotlinx.serialization.Serializable

@Serializable
data class ApiErrorResponse(
    val message: String? = null,
    val error: String? = null,
    val code: String? = null
)
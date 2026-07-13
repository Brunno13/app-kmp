package com.brunno.appkmp.data.remote

import com.brunno.appkmp.data.remote.models.LoginRequest
import com.brunno.appkmp.data.remote.models.LoginResponse
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.POST

interface AuthApi {

    @POST("api/auth/sign-in/email")
    suspend fun login(@Body request: LoginRequest): LoginResponse

}
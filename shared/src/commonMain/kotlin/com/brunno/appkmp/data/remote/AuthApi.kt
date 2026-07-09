package com.brunno.appkmp.data.remote

import com.brunno.appkmp.data.remote.models.LoginRequest
import com.brunno.appkmp.data.remote.models.LoginResponse
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.POST

interface AuthApi {

    @POST("sign-in")
    suspend fun login(@Body request: LoginRequest): LoginResponse

}
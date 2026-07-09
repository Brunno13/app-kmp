package com.brunno.appkmp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
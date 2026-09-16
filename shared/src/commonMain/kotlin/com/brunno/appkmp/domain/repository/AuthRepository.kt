package com.brunno.appkmp.domain.repository

interface AuthRepository :
    AuthenticationRepository,
    ProfileRepository,
    SecurityRepository

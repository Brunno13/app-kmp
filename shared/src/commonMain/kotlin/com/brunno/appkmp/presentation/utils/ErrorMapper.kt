package com.brunno.appkmp.presentation.utils

import androidx.compose.runtime.Composable
import com.brunno.appkmp.domain.error.AppError
import com.brunno.appkmp.domain.error.AuthError
import com.brunno.appkmp.domain.error.NetworkError
import kmpprojectbrunno.shared.generated.resources.Res
import kmpprojectbrunno.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun AppError.asString(): String {
    return when (this) {
        AuthError.INVALID_CREDENTIALS -> stringResource(Res.string.error_invalid_credentials)
        AuthError.USER_NOT_FOUND -> stringResource(Res.string.error_invalid_credentials)
        AuthError.UNAUTHORIZED -> stringResource(Res.string.error_invalid_credentials)
        AuthError.INVALID_PASSWORD -> stringResource(Res.string.error_invalid_password)
        AuthError.PASSWORD_TOO_SHORT -> stringResource(Res.string.error_password_too_short)
        NetworkError.NO_INTERNET -> stringResource(Res.string.error_no_internet)
        NetworkError.SERVER_ERROR -> stringResource(Res.string.error_server)
        NetworkError.TOO_MANY_REQUESTS -> stringResource(Res.string.error_too_many_requests)
        NetworkError.UNKNOWN -> stringResource(Res.string.error_unknown)

        else -> stringResource(Res.string.error_unknown)
    }
}
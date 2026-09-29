package com.mutissx.napptilusrickandmorty.core.data

import com.mutissx.napptilusrickandmorty.core.domain.DataError
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

fun Throwable.toNetworkError(): DataError.Network = when (this) {
    is SocketTimeoutException -> DataError.Network.REQUEST_TIMEOUT
    is IOException -> DataError.Network.NO_INTERNET
    is SerializationException -> DataError.Network.SERIALIZATION
    is HttpException -> when (code()) {
        401 -> DataError.Network.UNAUTHORIZED
        404 -> DataError.Network.NOT_FOUND
        in 500..599 -> DataError.Network.SERVICE_UNAVAILABLE
        in 400..499 -> DataError.Network.CLIENT_ERROR
        else -> DataError.Network.UNKNOWN
    }
    else -> DataError.Network.UNKNOWN
}

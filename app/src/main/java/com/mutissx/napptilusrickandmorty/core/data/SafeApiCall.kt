package com.mutissx.napptilusrickandmorty.core.data

import com.mutissx.napptilusrickandmorty.core.domain.DataError
import com.mutissx.napptilusrickandmorty.core.domain.Result
import kotlinx.coroutines.CancellationException

suspend inline fun <T> safeApiCall(block: suspend () -> T): Result<T, DataError.Network> =
    try {
        Result.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.Error(e.toNetworkError())
    }

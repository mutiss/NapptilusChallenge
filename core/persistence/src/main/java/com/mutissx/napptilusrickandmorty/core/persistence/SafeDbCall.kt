package com.mutissx.napptilusrickandmorty.core.persistence

import com.mutissx.napptilusrickandmorty.core.common.DataError
import com.mutissx.napptilusrickandmorty.core.common.Result
import kotlinx.coroutines.CancellationException

suspend inline fun <T> safeDbCall(block: suspend () -> T): Result<T, DataError.Local> =
    try {
        Result.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.Error(e.toLocalError())
    }

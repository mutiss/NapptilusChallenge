package com.mutissx.napptilusrickandmorty.core.persistence

import android.database.sqlite.SQLiteFullException
import com.mutissx.napptilusrickandmorty.core.common.DataError

fun Throwable.toLocalError(): DataError.Local = when (this) {
    is SQLiteFullException -> DataError.Local.DISK_FULL
    else -> DataError.Local.UNKNOWN
}

package com.mutissx.napptilusrickandmorty.core.ui.extensions

import com.mutissx.napptilusrickandmorty.core.ui.R
import com.mutissx.napptilusrickandmorty.core.common.DataError
import com.mutissx.napptilusrickandmorty.core.ui.UiText

fun DataError.asUiText(): UiText {
    return when (this) {
        DataError.Network.NO_INTERNET -> UiText.StringResource(R.string.no_internet)
        DataError.Network.NOT_FOUND -> UiText.StringResource(R.string.not_found)
        DataError.Network.SERVICE_UNAVAILABLE -> UiText.StringResource(R.string.server_error)
        DataError.Network.UNAUTHORIZED -> UiText.StringResource(R.string.unauthorized)
        DataError.Network.REQUEST_TIMEOUT -> UiText.StringResource(R.string.timed_out)
        DataError.Network.SERIALIZATION,
        DataError.Network.CLIENT_ERROR,
        DataError.Network.UNKNOWN -> UiText.StringResource(R.string.unknown_error)

        DataError.Local.DISK_FULL,
        DataError.Local.UNKNOWN -> UiText.StringResource(R.string.local_storage_error)
    }
}

package com.mutissx.napptilusrickandmorty.feature.characters.fake

import com.mutissx.napptilusrickandmorty.core.common.ConnectivityObserver
import kotlinx.coroutines.flow.MutableStateFlow

class FakeConnectivityObserver(online: Boolean = true) : ConnectivityObserver {

    override val isOnline = MutableStateFlow(online)

    fun setOnline(online: Boolean) {
        isOnline.value = online
    }
}

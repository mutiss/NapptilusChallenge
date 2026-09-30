package com.mutissx.napptilusrickandmorty.fake

import com.mutissx.napptilusrickandmorty.domain.repository.ConnectivityObserver
import kotlinx.coroutines.flow.MutableStateFlow

class FakeConnectivityObserver(online: Boolean = true) : ConnectivityObserver {

    override val isOnline = MutableStateFlow(online)

    fun setOnline(online: Boolean) {
        isOnline.value = online
    }
}

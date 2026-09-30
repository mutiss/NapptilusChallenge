package com.mutissx.napptilusrickandmorty.domain.repository

import kotlinx.coroutines.flow.Flow

interface ConnectivityObserver {
    /** Emits whether the device currently has a validated internet connection. */
    val isOnline: Flow<Boolean>
}

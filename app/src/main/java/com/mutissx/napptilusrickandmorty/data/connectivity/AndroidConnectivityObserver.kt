package com.mutissx.napptilusrickandmorty.data.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.core.content.getSystemService
import com.mutissx.napptilusrickandmorty.domain.repository.ConnectivityObserver
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Tracks the default network. "Online" means the network is validated (it actually reaches the
 * internet), so a Wi-Fi without connectivity or a captive portal counts as offline.
 */
class AndroidConnectivityObserver(context: Context) : ConnectivityObserver {

    private val connectivityManager: ConnectivityManager = requireNotNull(context.getSystemService())

    override val isOnline: Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                trySend(capabilities.hasValidatedInternet())
            }

            override fun onLost(network: Network) {
                trySend(false)
            }
        }
        // The callback only fires when there is a network, so seed the current state first.
        trySend(
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                ?.hasValidatedInternet() == true
        )
        connectivityManager.registerDefaultNetworkCallback(callback)
        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()

    private fun NetworkCapabilities.hasValidatedInternet(): Boolean =
        hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}

package com.mutissx.napptilusrickandmorty.di

import androidx.lifecycle.SavedStateHandle
import com.mutissx.napptilusrickandmorty.core.network.di.networkModule
import com.mutissx.napptilusrickandmorty.feature.characters.di.charactersModule
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.test.verify.verify

/**
 * Statically verifies that every constructor dependency declared across all modules' Koin
 * definitions resolves to a binding somewhere in the combined graph, without instantiating
 * anything (so it needs no Android Context). It lives in :app because that's the only place that
 * sees every module: it catches a forgotten binding or a class moved between modules at test
 * time instead of as an app-launch crash.
 *
 * Whitelisted extra types, all false positives from verify()'s constructor reflection rather
 * than real gaps in our bindings:
 * - `SavedStateHandle` is supplied at runtime by koinViewModel() (`it.get()` in the detail
 *   ViewModel's `viewModel { }` block), not by a module binding.
 * - `HttpLoggingInterceptor.Logger` belongs to a constructor overload we never call.
 * - `Int` is the optional max size of CharacterMemoryCache, left at its default.
 * - `Long` and `Function1` are RateLimitRetryInterceptor's optional max wait and sleep function
 *   (the latter only swapped in tests), both left at their defaults.
 * - `List` is CacheControlInterceptor's policies, gathered with getAll() from every feature's
 *   CachePolicies binding rather than bound as a List.
 */
@OptIn(KoinExperimentalAPI::class)
class KoinModulesTest {

    @Test
    fun `given all app Koin modules combined, when verified, then every dependency resolves`() {
        module {
            includes(networkModule, charactersModule)
        }.verify(
            extraTypes = listOf(
                SavedStateHandle::class,
                HttpLoggingInterceptor.Logger::class,
                Int::class,
                Long::class,
                Function1::class,
                List::class
            )
        )
    }
}

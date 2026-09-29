package com.mutissx.napptilusrickandmorty.di

import androidx.lifecycle.SavedStateHandle
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.test.verify.verify

/**
 * Statically verifies that every constructor dependency declared across the app's Koin
 * modules resolves to a binding somewhere in the combined graph, without instantiating
 * anything (so it needs no Android Context). Catches DI wiring mistakes — a forgotten
 * binding, a class moved between modules — at test time instead of as an app-launch crash.
 *
 * Whitelisted extra types, all false positives from verify()'s constructor reflection rather
 * than real gaps in our bindings:
 * - `SavedStateHandle` is supplied at runtime by koinViewModel() (`it.get()` in the detail
 *   ViewModel's `viewModel { }` block), not by a module binding.
 * - `HttpLoggingInterceptor.Logger` belongs to a constructor overload we never call.
 * - `Int` is the optional max size of CharacterMemoryCache, left at its default.
 */
@OptIn(KoinExperimentalAPI::class)
class KoinModulesTest {

    @Test
    fun `given all app Koin modules combined, when verified, then every dependency resolves`() {
        module {
            includes(dataModule, databaseModule, domainModule, networkModule, presentationModule)
        }.verify(
            extraTypes = listOf(
                SavedStateHandle::class,
                HttpLoggingInterceptor.Logger::class,
                Int::class
            )
        )
    }
}

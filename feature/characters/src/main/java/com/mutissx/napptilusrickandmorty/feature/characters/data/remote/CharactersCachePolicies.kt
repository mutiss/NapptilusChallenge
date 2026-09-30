package com.mutissx.napptilusrickandmorty.feature.characters.data.remote

import com.mutissx.napptilusrickandmorty.core.network.interceptor.CachePolicies
import com.mutissx.napptilusrickandmorty.core.network.interceptor.CachePolicy
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

/**
 * Cache lifetimes of the Rick and Morty endpoints this feature uses (first match wins):
 * - Episodes: 7 days (aired episodes never change).
 * - Single character: 1 day.
 * - Character pages: 1 hour (new characters are rare, but a list should not look frozen).
 */
val charactersCachePolicies = CachePolicies(
    listOf(
        CachePolicy(pathPattern = Regex("/episode/"), maxAge = 7.days),
        CachePolicy(pathPattern = Regex("""/character/\d+$"""), maxAge = 1.days),
        CachePolicy(pathPattern = Regex("/character"), maxAge = 1.hours)
    )
)

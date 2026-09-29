package com.mutissx.napptilusrickandmorty.data.cache

import com.mutissx.napptilusrickandmorty.domain.model.Character

/**
 * Small in-memory LRU of characters already fetched by the list.
 *
 * The list endpoint returns the full character payload, so opening a character's detail
 * from the list never needs a second network round-trip: the repository answers from here
 * without suspending. Besides saving a request, that makes the detail content available on
 * the very first frames, which is what lets the shared-element image transition line up.
 */
class CharacterMemoryCache(private val maxSize: Int = DEFAULT_MAX_SIZE) {

    private val entries = object : LinkedHashMap<Int, Character>(INITIAL_CAPACITY, LOAD_FACTOR, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Int, Character>?): Boolean =
            size > maxSize
    }

    @Synchronized
    fun get(id: Int): Character? = entries[id]

    @Synchronized
    fun put(character: Character) {
        entries[character.id] = character
    }

    @Synchronized
    fun putAll(characters: List<Character>) {
        characters.forEach { entries[it.id] = it }
    }

    companion object {
        const val DEFAULT_MAX_SIZE = 500
        private const val INITIAL_CAPACITY = 64
        private const val LOAD_FACTOR = 0.75f
    }
}

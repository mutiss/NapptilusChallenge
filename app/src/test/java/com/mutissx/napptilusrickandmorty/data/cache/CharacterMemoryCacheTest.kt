package com.mutissx.napptilusrickandmorty.data.cache

import com.mutissx.napptilusrickandmorty.fake.aCharacter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class CharacterMemoryCacheTest {

    @Test
    fun `given characters were put, when get is called with their id, then they are returned`() {
        val cache = CharacterMemoryCache()
        val rick = aCharacter(id = 1)
        val morty = aCharacter(id = 2, name = "Morty Smith")

        cache.putAll(listOf(rick, morty))

        assertEquals(rick, cache.get(1))
        assertEquals(morty, cache.get(2))
    }

    @Test
    fun `given an id that was never put, when get is called, then returns null`() {
        assertNull(CharacterMemoryCache().get(42))
    }

    @Test
    fun `given the cache is full, when a new character is put, then the least recently used one is evicted`() {
        val cache = CharacterMemoryCache(maxSize = 2)
        cache.put(aCharacter(id = 1))
        cache.put(aCharacter(id = 2))
        cache.get(1) // touch 1 so 2 becomes the eldest

        cache.put(aCharacter(id = 3))

        assertNotNull(cache.get(1))
        assertNull(cache.get(2))
        assertNotNull(cache.get(3))
    }
}

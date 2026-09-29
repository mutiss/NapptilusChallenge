package com.mutissx.napptilusrickandmorty.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FavoriteCharacterDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: FavoriteCharacterDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()
        dao = database.favoriteCharacterDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun entity(id: Int, addedAt: Long, name: String = "Character $id") = FavoriteCharacterEntity(
        id = id,
        name = name,
        status = "ALIVE",
        species = "Human",
        type = null,
        gender = "MALE",
        origin = "Earth (C-137)",
        location = null,
        imageUrl = "",
        episodeIds = "1,2",
        addedAt = addedAt
    )

    @Test
    fun given_multiple_entities_inserted_when_observeAll_is_collected_then_they_are_ordered_by_addedAt_descending() =
        runTest {
            // Given
            dao.insert(entity(1, addedAt = 100L))
            dao.insert(entity(2, addedAt = 300L))
            dao.insert(entity(3, addedAt = 200L))

            // When / Then
            dao.observeAll().test {
                assertEquals(listOf(2, 3, 1), awaitItem().map { it.id })
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun given_an_entity_with_the_same_id_when_inserted_again_then_it_replaces_the_existing_row() =
        runTest {
            // Given
            dao.insert(entity(1, addedAt = 100L, name = "Original Name"))

            // When
            dao.insert(entity(1, addedAt = 100L, name = "Updated Name"))

            // Then
            dao.observeAll().test {
                val result = awaitItem()
                assertEquals(1, result.size)
                assertEquals("Updated Name", result.first().name)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun given_no_matching_row_when_observeIsFavorite_is_collected_then_it_emits_false() =
        runTest {
            dao.observeIsFavorite(42).test {
                assertFalse(awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun given_a_matching_row_when_observeIsFavorite_is_collected_then_it_emits_true() =
        runTest {
            // Given
            dao.insert(entity(1, addedAt = 100L))

            // When / Then
            dao.observeIsFavorite(1).test {
                assertTrue(awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun given_an_inserted_entity_when_getById_is_called_then_it_is_returned_and_missing_ids_return_null() =
        runTest {
            // Given
            val stored = entity(1, addedAt = 100L)
            dao.insert(stored)

            // When / Then
            assertEquals(stored, dao.getById(1))
            assertNull(dao.getById(2))
        }

    @Test
    fun given_an_inserted_entity_when_delete_is_called_then_observeAll_reflects_its_removal() =
        runTest {
            // Given
            dao.insert(entity(1, addedAt = 100L))

            dao.observeAll().test {
                assertEquals(1, awaitItem().size)

                // When
                dao.delete(1)

                // Then
                assertEquals(0, awaitItem().size)
                cancelAndIgnoreRemainingEvents()
            }
        }
}

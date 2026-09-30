package com.mutissx.napptilusrickandmorty.feature.characters.domain.usecase

import androidx.paging.PagingData
import com.mutissx.napptilusrickandmorty.feature.characters.domain.model.Character
import com.mutissx.napptilusrickandmorty.feature.characters.domain.model.CharacterFilter
import com.mutissx.napptilusrickandmorty.feature.characters.domain.model.CharacterStatus
import com.mutissx.napptilusrickandmorty.feature.characters.domain.repository.CharacterRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertSame
import org.junit.Test

class SearchCharactersUseCaseTest {

    private val repository: CharacterRepository = mockk()
    private val useCase = SearchCharactersUseCase(repository)

    @Test
    fun `given a filter, when invoke is called, then the repository receives that exact filter and its flow is returned`() {
        // Given
        val filter = CharacterFilter(name = "rick", status = CharacterStatus.ALIVE)
        val pagingFlow: Flow<PagingData<Character>> = flowOf(PagingData.empty())
        every { repository.getCharacters(filter) } returns pagingFlow

        // When
        val result = useCase(filter)

        // Then
        assertSame(pagingFlow, result)
        verify(exactly = 1) { repository.getCharacters(filter) }
    }
}

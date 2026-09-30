package com.mutissx.napptilusrickandmorty.feature.characters.presentation.detail.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mutissx.napptilusrickandmorty.feature.characters.R
import com.mutissx.napptilusrickandmorty.core.common.Result
import com.mutissx.napptilusrickandmorty.core.ui.UiText
import com.mutissx.napptilusrickandmorty.core.ui.extensions.asUiText
import com.mutissx.napptilusrickandmorty.feature.characters.domain.model.Character
import com.mutissx.napptilusrickandmorty.feature.characters.domain.usecase.GetCharacterDetailUseCase
import com.mutissx.napptilusrickandmorty.feature.characters.domain.usecase.GetCharacterEpisodesUseCase
import com.mutissx.napptilusrickandmorty.feature.characters.domain.usecase.ObserveIsFavoriteUseCase
import com.mutissx.napptilusrickandmorty.feature.characters.domain.usecase.ToggleFavoriteUseCase
import com.mutissx.napptilusrickandmorty.feature.characters.presentation.detail.state.CharacterDetailUiState
import com.mutissx.napptilusrickandmorty.feature.characters.presentation.detail.state.CharacterSectionState
import com.mutissx.napptilusrickandmorty.feature.characters.presentation.detail.state.EpisodesState
import com.mutissx.napptilusrickandmorty.feature.characters.navigation.Destination
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CharacterDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val getCharacterDetailUseCase: GetCharacterDetailUseCase,
    private val getCharacterEpisodesUseCase: GetCharacterEpisodesUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase,
    isFavoriteUseCase: ObserveIsFavoriteUseCase
) : ViewModel() {

    // Safe today: "id" is a required, typed (IntType) path segment with a single call site and
    // the manifest declares no deep links, so this can only fail via a misconfigured nav graph.
    // If a deep link is ever added, surface a CharacterDetailUiState.Error instead of crashing,
    // since the id would then come from untrusted external input.
    private val characterId: Int = requireNotNull(savedStateHandle[Destination.CharacterDetail.ARG_ID]) {
        "character id missing from arguments"
    }

    private val _characterState = MutableStateFlow<CharacterSectionState>(CharacterSectionState.Loading)
    private val _episodesState = MutableStateFlow<EpisodesState>(EpisodesState.Loading)

    // The character and its episodes are separate sections: an episodes failure must not hide
    // a character that loaded fine, and each section retries on its own.
    val uiState: StateFlow<CharacterDetailUiState> =
        combine(
            _characterState,
            _episodesState,
            isFavoriteUseCase(characterId)
        ) { character, episodes, isFavorite ->
            when (character) {
                CharacterSectionState.Loading -> CharacterDetailUiState.Loading
                is CharacterSectionState.Error -> CharacterDetailUiState.Error(character.message)
                is CharacterSectionState.Loaded -> CharacterDetailUiState.Content(
                    character = character.data,
                    isFavorite = isFavorite,
                    episodes = episodes
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CharacterDetailUiState.Loading
        )

    private val _favoriteMessages = Channel<UiText>(Channel.BUFFERED)
    val favoriteMessages: Flow<UiText> = _favoriteMessages.receiveAsFlow()

    init {
        loadInfo()
    }

    fun loadInfo() {
        _characterState.value = CharacterSectionState.Loading
        _episodesState.value = EpisodesState.Loading
        viewModelScope.launch {
            when (val result = getCharacterDetailUseCase(characterId)) {
                is Result.Error -> _characterState.value = CharacterSectionState.Error(result.error.asUiText())
                is Result.Success -> {
                    _characterState.value = CharacterSectionState.Loaded(result.data)
                    // Episodes depend on the character's episode ids, so they chain after it.
                    loadEpisodes(result.data)
                }
            }
        }
    }

    fun retryEpisodes() {
        val character = (_characterState.value as? CharacterSectionState.Loaded)?.data ?: return
        viewModelScope.launch { loadEpisodes(character) }
    }

    private suspend fun loadEpisodes(character: Character) {
        _episodesState.value = EpisodesState.Loading
        _episodesState.value = when (val result = getCharacterEpisodesUseCase(character)) {
            is Result.Error -> EpisodesState.Error(result.error.asUiText())
            is Result.Success -> EpisodesState.Loaded(result.data)
        }
    }

    fun onFavoriteToggle() {
        val current = uiState.value as? CharacterDetailUiState.Content ?: return
        viewModelScope.launch {
            val result = toggleFavorite(current.character, current.isFavorite)
            val message = if (result is Result.Error) {
                // A feature-specific message beats core's generic "couldn't save" for local errors.
                UiText.StringResource(R.string.favorite_toggle_error)
            } else {
                val messageRes =
                    if (current.isFavorite) R.string.favorite_removed else R.string.favorite_added
                UiText.StringResource(messageRes, current.character.name)
            }
            _favoriteMessages.send(message)
        }
    }
}

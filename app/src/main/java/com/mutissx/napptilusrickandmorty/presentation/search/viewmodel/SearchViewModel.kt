package com.mutissx.napptilusrickandmorty.presentation.search.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.mutissx.napptilusrickandmorty.R
import com.mutissx.napptilusrickandmorty.core.domain.DataException
import com.mutissx.napptilusrickandmorty.core.ui.UiText
import com.mutissx.napptilusrickandmorty.core.ui.extensions.asUiText
import com.mutissx.napptilusrickandmorty.domain.model.Character
import com.mutissx.napptilusrickandmorty.domain.model.CharacterFilter
import com.mutissx.napptilusrickandmorty.domain.model.CharacterGender
import com.mutissx.napptilusrickandmorty.domain.model.CharacterStatus
import com.mutissx.napptilusrickandmorty.domain.usecase.SearchCharactersUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SearchViewModel(
    private val searchCharactersUseCase: SearchCharactersUseCase
) : ViewModel() {

    private val _query = MutableStateFlow(EMPTY_VALUE)
    val query: StateFlow<String> = _query.asStateFlow()

    private val _status = MutableStateFlow<CharacterStatus?>(null)
    val status: StateFlow<CharacterStatus?> = _status.asStateFlow()

    private val _gender = MutableStateFlow<CharacterGender?>(null)
    val gender: StateFlow<CharacterGender?> = _gender.asStateFlow()

    // Only typing is debounced: clearing the field or tapping a filter chip is a deliberate
    // action and should refresh the grid immediately.
    private val debouncedName: Flow<String> = _query
        .map { query -> query.trim() }
        .debounce { query -> if (query.isEmpty()) 0L else SEARCH_DEBOUNCE_MS }
        .distinctUntilChanged()

    val results: Flow<PagingData<Character>> =
        combine(debouncedName, _status, _gender) { name, status, gender ->
            CharacterFilter(name = name, status = status, gender = gender)
        }
            .distinctUntilChanged()
            .flatMapLatest { filter -> searchCharactersUseCase(filter) }
            .cachedIn(viewModelScope)

    fun onQueryChange(value: String) {
        _query.value = value
    }

    fun onClearQuery() {
        _query.value = EMPTY_VALUE
    }

    /** Tapping the selected chip again clears that filter. */
    fun onStatusSelected(status: CharacterStatus) {
        _status.update { current -> if (current == status) null else status }
    }

    fun onGenderSelected(gender: CharacterGender) {
        _gender.update { current -> if (current == gender) null else gender }
    }

    fun errorMessage(throwable: Throwable): UiText =
        (throwable as? DataException)?.error?.asUiText()
            ?: UiText.StringResource(R.string.search_error_generic)

    companion object {
        const val EMPTY_VALUE = ""
        const val SEARCH_DEBOUNCE_MS = 400L
    }
}

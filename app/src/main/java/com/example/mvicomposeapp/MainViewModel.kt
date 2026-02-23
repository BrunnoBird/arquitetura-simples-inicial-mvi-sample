package com.example.mvicomposeapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mvicomposeapp.domain.SubmitNameResult
import com.example.mvicomposeapp.domain.SubmitNameUseCase
import com.example.mvicomposeapp.mvi.UiEffect
import com.example.mvicomposeapp.mvi.UiIntent
import com.example.mvicomposeapp.mvi.UiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * MVI "single source of truth":
 * - UIState: snapshot imutável do que a UI precisa renderizar
 * - Intent: ações/eventos que entram no ViewModel
 * - Effect: eventos one-off (snackbar, navegação, etc). NÃO ficam no state.
 */
class MainViewModel(
    private val submitNameUseCase: SubmitNameUseCase,
    private val workerDispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()

    private val _effect = Channel<UiEffect>(capacity = Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: UiIntent) {
        when (intent) {
            UiIntent.Increment -> reduce { copy(count = count + 1) }
            UiIntent.Decrement -> reduce { copy(count = count - 1) }
            UiIntent.ClearMessage -> reduce { copy(message = "") }

            is UiIntent.NameChanged -> reduce { copy(name = intent.value) }

            UiIntent.SubmitName -> {
                if (!state.value.isLoading) submitName()
            }

            UiIntent.RequestNavigate -> emitEffect(UiEffect.NavigateToDetails)
        }
    }

    private fun submitName() {
        viewModelScope.launch(workerDispatcher) {
            reduce { copy(isLoading = true) }
            when (val result = submitNameUseCase.execute(state.value.name)) {
                SubmitNameResult.EmptyName -> {
                    reduce { copy(isLoading = false) }
                    emitEffect(UiEffect.ShowSnackbar("Digite um nome antes de enviar."))
                }

                is SubmitNameResult.Success -> {
                    reduce {
                        copy(
                            isLoading = false,
                            message = result.stateMessage
                        )
                    }
                    emitEffect(UiEffect.ShowSnackbar(result.snackbarMessage))
                }
            }
        }
    }

    private fun reduce(transform: UiState.() -> UiState) {
        _state.update { it.transform() }
    }

    private fun emitEffect(effect: UiEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}

class MainViewModelFactory(
    private val submitNameUseCase: SubmitNameUseCase,
    private val workerDispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        check(modelClass.isAssignableFrom(MainViewModel::class.java)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        return MainViewModel(submitNameUseCase, workerDispatcher) as T
    }
}

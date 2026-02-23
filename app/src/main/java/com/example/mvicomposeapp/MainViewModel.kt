package com.example.mvicomposeapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
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
class MainViewModel : ViewModel() {

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

            UiIntent.SubmitName -> submitName()

            UiIntent.RequestNavigate -> emitEffect(UiEffect.NavigateToDetails)
        }
    }

    private fun submitName() {
        val currentName = state.value.name.trim()
        if (currentName.isEmpty()) {
            emitEffect(UiEffect.ShowSnackbar("Digite um nome antes de enviar."))
            return
        }

        viewModelScope.launch {
            reduce { copy(isLoading = true) }
            delay(700) // simula chamada de rede
            reduce {
                copy(
                    isLoading = false,
                    message = "Olá, $currentName! (salvo com sucesso)"
                )
            }
            emitEffect(UiEffect.ShowSnackbar("Nome enviado: $currentName"))
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

data class UiState(
    val count: Int = 0,
    val name: String = "",
    val message: String = "",
    val isLoading: Boolean = false
)

sealed interface UiIntent {
    data object Increment : UiIntent
    data object Decrement : UiIntent
    data class NameChanged(val value: String) : UiIntent
    data object SubmitName : UiIntent
    data object ClearMessage : UiIntent

    /** exemplo de evento que não deve virar state, e sim Effect (navegação) */
    data object RequestNavigate : UiIntent
}

sealed interface UiEffect {
    data class ShowSnackbar(val message: String) : UiEffect
    data object NavigateToDetails : UiEffect
}

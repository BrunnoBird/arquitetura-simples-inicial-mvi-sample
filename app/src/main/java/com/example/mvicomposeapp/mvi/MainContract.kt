package com.example.mvicomposeapp.mvi

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
    data object RequestNavigate : UiIntent
}

sealed interface UiEffect {
    data class ShowSnackbar(val message: String) : UiEffect
    data object NavigateToDetails : UiEffect
}

package com.example.mvicomposeapp.domain

import kotlinx.coroutines.delay

interface SubmitNameUseCase {
    suspend fun execute(rawName: String): SubmitNameResult
}

sealed interface SubmitNameResult {
    data object EmptyName : SubmitNameResult
    data class Success(
        val stateMessage: String,
        val snackbarMessage: String
    ) : SubmitNameResult
}

class DefaultSubmitNameUseCase(
    private val delayMs: Long = 700L
) : SubmitNameUseCase {
    override suspend fun execute(rawName: String): SubmitNameResult {
        val name = rawName.trim()
        if (name.isEmpty()) return SubmitNameResult.EmptyName

        delay(delayMs)
        return SubmitNameResult.Success(
            stateMessage = "Olá, $name! (salvo com sucesso)",
            snackbarMessage = "Nome enviado: $name"
        )
    }
}

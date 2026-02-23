package com.example.mvicomposeapp.domain

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultSubmitNameUseCaseTest {

    @Test
    fun `returns EmptyName when input is blank`() = runTest {
        val useCase = DefaultSubmitNameUseCase(delayMs = 700)

        val result = useCase.execute("   ")

        assertTrue(result is SubmitNameResult.EmptyName)
    }

    @Test
    fun `returns Success with trimmed name and waits configured delay`() = runTest {
        val useCase = DefaultSubmitNameUseCase(delayMs = 700)
        var result: SubmitNameResult? = null

        backgroundScope.launch {
            result = useCase.execute("  Bruno  ")
        }
        runCurrent()
        assertEquals(null, result)

        advanceTimeBy(700)
        runCurrent()

        assertEquals(
            SubmitNameResult.Success(
                stateMessage = "Olá, Bruno! (salvo com sucesso)",
                snackbarMessage = "Nome enviado: Bruno"
            ),
            result
        )
    }
}

package com.example.mvicomposeapp

import com.example.mvicomposeapp.domain.SubmitNameResult
import com.example.mvicomposeapp.domain.SubmitNameUseCase
import com.example.mvicomposeapp.mvi.UiEffect
import com.example.mvicomposeapp.mvi.UiIntent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    @Test
    fun `increment and decrement update counter state`() = runTest {
        val viewModel = MainViewModel(
            submitNameUseCase = FakeSubmitNameUseCase(result = SubmitNameResult.EmptyName),
            workerDispatcher = testDispatcher
        )

        viewModel.onIntent(UiIntent.Increment)
        viewModel.onIntent(UiIntent.Increment)
        viewModel.onIntent(UiIntent.Decrement)

        advanceUntilIdle()
        assertEquals(1, viewModel.state.value.count)
    }

    @Test
    fun `submit with empty name emits validation effect`() = runTest {
        val viewModel = MainViewModel(
            submitNameUseCase = FakeSubmitNameUseCase(result = SubmitNameResult.EmptyName),
            workerDispatcher = testDispatcher
        )
        val effectDeferred = async { viewModel.effect.first() }

        viewModel.onIntent(UiIntent.SubmitName)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
        assertEquals(
            UiEffect.ShowSnackbar("Digite um nome antes de enviar."),
            effectDeferred.await()
        )
    }

    @Test
    fun `submit success toggles loading updates message and emits success effect`() = runTest {
        val viewModel = MainViewModel(
            submitNameUseCase = FakeSubmitNameUseCase(
                delayMs = 700,
                result = SubmitNameResult.Success(
                    stateMessage = "Olá, Ana! (salvo com sucesso)",
                    snackbarMessage = "Nome enviado: Ana"
                )
            ),
            workerDispatcher = testDispatcher
        )
        val effectDeferred = async { viewModel.effect.first() }

        viewModel.onIntent(UiIntent.NameChanged(" Ana "))
        viewModel.onIntent(UiIntent.SubmitName)
        runCurrent()

        assertTrue(viewModel.state.value.isLoading)

        advanceTimeBy(700)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
        assertEquals("Olá, Ana! (salvo com sucesso)", viewModel.state.value.message)
        assertEquals(UiEffect.ShowSnackbar("Nome enviado: Ana"), effectDeferred.await())
    }

    private class FakeSubmitNameUseCase(
        private val delayMs: Long = 0L,
        private val result: SubmitNameResult
    ) : SubmitNameUseCase {
        override suspend fun execute(rawName: String): SubmitNameResult {
            if (delayMs > 0) delay(delayMs)
            return result
        }
    }
}

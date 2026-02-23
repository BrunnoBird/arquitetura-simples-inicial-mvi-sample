package com.example.mvicomposeapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest
import com.example.mvicomposeapp.MainViewModel
import com.example.mvicomposeapp.mvi.UiEffect
import com.example.mvicomposeapp.mvi.UiIntent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MviScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    val snackbarHostState = remember { SnackbarHostState() }

    // ✅ Boa prática: Effects são coletados em LaunchedEffect (eventos one-off).
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is UiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                UiEffect.NavigateToDetails -> snackbarHostState.showSnackbar("Exemplo: navegar para Details")
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("MVI + Compose") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text("Counter: ${state.count}")

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { viewModel.onIntent(UiIntent.Decrement) }) { Text("-1") }
                Button(onClick = { viewModel.onIntent(UiIntent.Increment) }) { Text("+1") }
            }

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = state.name,
                onValueChange = { viewModel.onIntent(UiIntent.NameChanged(it)) },
                label = { Text("Seu nome") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { viewModel.onIntent(UiIntent.SubmitName) },
                    enabled = !state.isLoading
                ) {
                    Text("Enviar")
                }

                Button(onClick = { viewModel.onIntent(UiIntent.RequestNavigate) }) {
                    Text("Navegar (Effect)")
                }

                if (state.isLoading) {
                    CircularProgressIndicator()
                }
            }

            if (state.message.isNotBlank()) {
                Text("Mensagem (UIState): ${state.message}")
                Button(onClick = { viewModel.onIntent(UiIntent.ClearMessage) }) {
                    Text("Limpar mensagem")
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                "Notas rápidas:
" +
                    "• UIState guarda apenas o que a tela precisa renderizar.
" +
                    "• Intent representa ações/eventos.
" +
                    "• Effect é para eventos de uso único (snackbar, navegação, etc)."
            )
        }
    }
}

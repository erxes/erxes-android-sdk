package com.erxes.messenger.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.erxes.messenger.data.MessengerRepository.ContactKind

/** requireAuth gate: collect an email or phone (+ optional name) before starting a chat. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun IdentityFormScreen(
    onIdentified: () -> Unit,
    onBack: () -> Unit,
    viewModel: IdentityViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var kind by remember { mutableStateOf(ContactKind.EMAIL) }
    var value by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    LaunchedEffect(state.done) { if (state.done) onIdentified() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Before we start") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { inner ->
        Column(
            modifier = Modifier.fillMaxSize().padding(inner).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                "Please share a way to reach you so our team can follow up.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = kind == ContactKind.EMAIL,
                    onClick = { kind = ContactKind.EMAIL },
                    label = { Text("Email") },
                )
                FilterChip(
                    selected = kind == ContactKind.PHONE,
                    onClick = { kind = ContactKind.PHONE },
                    label = { Text("Phone") },
                )
            }
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                label = { Text(if (kind == ContactKind.EMAIL) "Email" else "Phone") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (kind == ContactKind.EMAIL) KeyboardType.Email else KeyboardType.Phone,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            Button(
                onClick = { viewModel.submit(kind, value, name) },
                enabled = value.isNotBlank() && !state.isSubmitting,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(end = 8.dp).size(18.dp),
                        strokeWidth = 2.dp,
                    )
                }
                Text("Continue")
            }
        }
    }
}

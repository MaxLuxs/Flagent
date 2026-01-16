package com.flagent.sample.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.flagent.sample.ui.components.SettingsCard
import com.flagent.sample.viewmodel.AuthType
import com.flagent.sample.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel()
) {
    val settings by viewModel.settings.collectAsState()
    
    var baseUrl by remember { mutableStateOf(settings.baseUrl) }
    var cacheEnabled by remember { mutableStateOf(settings.cacheEnabled) }
    var cacheTtlText by remember { mutableStateOf((settings.cacheTtlMs / 1000).toString()) }
    var authType by remember { mutableStateOf(settings.authType) }
    var username by remember { mutableStateOf(settings.authUsername) }
    var password by remember { mutableStateOf(settings.authPassword) }
    var token by remember { mutableStateOf(settings.authToken) }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SettingsCard(title = "API Configuration") {
                OutlinedTextField(
                    value = baseUrl,
                    onValueChange = { baseUrl = it },
                    label = { Text("Base URL") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Button(
                    onClick = { viewModel.updateBaseUrl(baseUrl) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save URL")
                }
            }

            SettingsCard(title = "Cache Settings") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enable Cache")
                    Switch(
                        checked = cacheEnabled,
                        onCheckedChange = {
                            cacheEnabled = it
                            viewModel.updateCacheEnabled(it)
                        }
                    )
                }
                
                OutlinedTextField(
                    value = cacheTtlText,
                    onValueChange = { 
                        if (it.all { char -> char.isDigit() }) {
                            cacheTtlText = it
                            it.toLongOrNull()?.let { seconds ->
                                viewModel.updateCacheTtl(seconds * 1000)
                            }
                        }
                    },
                    label = { Text("Cache TTL (seconds)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Button(
                    onClick = { viewModel.clearCache() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Clear Cache")
                }
            }

            SettingsCard(title = "Authentication") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(AuthType.NONE, AuthType.BASIC, AuthType.BEARER).forEach { type ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(type.name)
                            Switch(
                                checked = authType == type,
                                onCheckedChange = {
                                    if (it) {
                                        authType = type
                                        viewModel.updateAuthType(type)
                                    }
                                }
                            )
                        }
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                if (authType == AuthType.BASIC) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )
                    Button(
                        onClick = {
                            viewModel.updateAuthCredentials(username, password)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Credentials")
                    }
                }

                if (authType == AuthType.BEARER) {
                    OutlinedTextField(
                        value = token,
                        onValueChange = { token = it },
                        label = { Text("Bearer Token") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            viewModel.updateAuthToken(token)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Token")
                    }
                }
            }
        }
    }
}

package com.erxes.messenger.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.erxes.messenger.ErxesMessenger
import com.erxes.messenger.config.DisplayMode
import com.erxes.messenger.config.MessengerConfig
import com.erxes.messenger.config.MessengerUser
import com.erxes.messenger.ui.launcher.ErxesMessengerHost

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mirrors the values used by the upstream iOS example
        // (Example/MessengerExample/MessengerExampleApp.swift).
        ErxesMessenger.configure(
            context = this,
            config = MessengerConfig(
                endpoint = "https://officenext.erxes.io",
                integrationId = "9S6seo9wawN6cou8v",
                // Switch to the AI-assistant-style shell. The floating launcher is
                // suppressed in chat mode; the "Open messenger" button opens it full-screen.
                displayMode = DisplayMode.CHAT,
            ),
        )

        // Identify the visitor so the conversation attaches to a known customer.
        ErxesMessenger.setUser(
            MessengerUser(email = "monkhorgilbayarbaatar@gmail.com"),
        )

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    // ErxesMessengerHost overlays a draggable launcher button that
                    // appears once the connect handshake succeeds.
                    ErxesMessengerHost {
                        SampleScreen(onOpen = { ErxesMessenger.show(this) })
                    }
                }
            }
        }
    }
}

@Composable
private fun SampleScreen(onOpen: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("erxes Messenger SDK — Sample", style = MaterialTheme.typography.titleLarge)
        Button(onClick = onOpen, modifier = Modifier.padding(top = 16.dp)) {
            Text("Open messenger")
        }
    }
}

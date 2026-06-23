package com.erxes.messenger

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import com.erxes.messenger.config.ActionItem
import com.erxes.messenger.config.DisplayMode
import com.erxes.messenger.config.MessengerConfig
import com.erxes.messenger.network.MessengerOperations
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatModeConfigTest {

    @Test
    fun `messenger config defaults leave chat actions empty`() {
        val config = MessengerConfig(endpoint = "https://app.example.io", integrationId = "int1")

        assertEquals(DisplayMode.CLASSIC, config.displayMode)
        assertTrue(config.homeActions.isEmpty())
        assertTrue(config.drawerActions.isEmpty())
    }

    @Test
    fun `messenger config stores chat mode action items`() {
        val home = ActionItem(id = "search", title = "Search", imageVector = Icons.Filled.Search)
        val drawer = ActionItem(id = "project", title = "Project", drawableRes = 42)
        val config = MessengerConfig(
            endpoint = "https://app.example.io",
            integrationId = "int1",
            displayMode = DisplayMode.CHAT,
            homeActions = listOf(home),
            drawerActions = listOf(drawer),
        )

        assertEquals(DisplayMode.CHAT, config.displayMode)
        assertSame(home, config.homeActions.single())
        assertSame(drawer, config.drawerActions.single())
        assertEquals(42, config.drawerActions.single().drawableRes)
    }

    @Test
    fun `conversation list operation requests online status for chat title`() {
        assertTrue(
            MessengerOperations.CONVERSATIONS.contains("participatedUsers { _id details { avatar fullName } isOnline }")
        )
    }
}

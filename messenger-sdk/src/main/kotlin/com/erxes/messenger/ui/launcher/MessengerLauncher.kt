package com.erxes.messenger.ui.launcher

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.erxes.messenger.ErxesMessenger
import com.erxes.messenger.config.DisplayMode
import com.erxes.messenger.ui.MessengerActivity
import kotlin.math.roundToInt

/**
 * Draggable launcher button that snaps to the nearest horizontal edge and opens the
 * messenger when tapped. Visible only after the connect handshake ([ErxesMessenger.isReady]).
 * Mirrors `MessengerLaunchButton` (iOS). Place inside [ErxesMessengerHost].
 */
@Composable
fun MessengerLauncher(modifier: Modifier = Modifier) {
    // In chat mode there is no floating launcher — the host opens the full-screen shell
    // via ErxesMessenger.show(). Mirrors iOS `showLauncher()` being a no-op in `.chat`.
    if (ErxesMessenger.config?.displayMode == DisplayMode.CHAT) return

    val isReady by ErxesMessenger.isReady.collectAsStateWithLifecycle()
    if (!isReady) return

    val context = LocalContext.current
    val density = LocalDensity.current

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val maxXpx = with(density) { (maxWidth - LauncherSize - Margin * 2).toPx() }
        val maxYpx = with(density) { (maxHeight - LauncherSize - Margin * 2).toPx() }
        val marginPx = with(density) { Margin.toPx() }

        var offsetX by remember { mutableFloatStateOf(maxXpx) }
        var offsetY by remember { mutableFloatStateOf(maxYpx) }

        FloatingActionButton(
            onClick = { context.startActivity(MessengerActivity.intent(context)) },
            shape = CircleShape,
            modifier = Modifier
                .offset { IntOffset((offsetX + marginPx).roundToInt(), (offsetY + marginPx).roundToInt()) }
                .size(LauncherSize)
                .pointerInput(maxXpx, maxYpx) {
                    detectDragGestures(
                        onDrag = { change, drag ->
                            change.consume()
                            offsetX = (offsetX + drag.x).coerceIn(0f, maxXpx)
                            offsetY = (offsetY + drag.y).coerceIn(0f, maxYpx)
                        },
                        onDragEnd = {
                            // Snap to the nearest horizontal edge.
                            offsetX = if (offsetX < maxXpx / 2) 0f else maxXpx
                        },
                    )
                },
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Open chat")
        }
    }
}

private val LauncherSize = 56.dp
private val Margin = 16.dp

/**
 * Overlays a [MessengerLauncher] on top of [content]. Use as the root of your screen to
 * get a floating launcher button.
 */
@Composable
fun ErxesMessengerHost(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        content()
        MessengerLauncher()
    }
}

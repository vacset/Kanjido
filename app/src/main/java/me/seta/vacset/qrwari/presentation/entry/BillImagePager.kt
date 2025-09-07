package me.seta.vacset.qrwari.presentation.entry

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BillImagePager( // Changed to fun (public)
    imageUris: List<String>,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { imageUris.size })

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f) // Pager takes available vertical space in its Column
                .fillMaxWidth()
        ) { pageIndex ->
            val uriString = imageUris[pageIndex]

            // State for zoom and pan, keyed to the image URI to reset on page change
            var scale by remember(uriString) { mutableStateOf(1f) }
            var offsetX by remember(uriString) { mutableStateOf(0f) }
            var offsetY by remember(uriString) { mutableStateOf(0f) }

            Box( // This Box acts as the viewport and gesture detector for each page
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp)) // Clip the content
                    .pointerInput(uriString) { // Key pointerInput to uriString to reset gesture state if needed
                        detectTransformGestures { centroid, pan, zoom, rotation ->
                            scale = (scale * zoom).coerceIn(1f, 3f) // Min 1x, Max 3x zoom

                            if (scale > 1f) { // Allow panning only when zoomed
                                offsetX += pan.x * scale
                                offsetY += pan.y * scale
                                // TODO: Add logic here to constrain offsetX and offsetY 
                                // to prevent panning the image out of view. This requires knowing image intrinsic size vs viewport size.
                            } else {
                                // If not zoomed (scale is 1f), reset offsets
                                offsetX = 0f
                                offsetY = 0f
                            }
                        }
                    }
            ) {
                // **IMPORTANT**: Replace this Box with a proper image loading Composable (e.g., Coil's AsyncImage)
                // The AsyncImage would have Modifier.graphicsLayer applied to it.
                // Example with Coil:
                // AsyncImage(
                //     model = Uri.parse(uriString),
                //     contentDescription = "Bill image ${pageIndex + 1}",
                //     contentScale = ContentScale.Fit, // Fit within bounds initially for graphicsLayer to scale from
                //     modifier = Modifier
                //         .fillMaxSize() // Align within the Box or use appropriate alignment
                //         .graphicsLayer(
                //             scaleX = scale,
                //             scaleY = scale,
                //             translationX = offsetX,
                //             translationY = offsetY
                //         )
                // )

                // Current Placeholder demonstrating the structure:
                Box(
                    modifier = Modifier
                        .fillMaxSize() // Placeholder content fills the viewport
                        .graphicsLayer( // Apply transformations to the placeholder
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offsetX,
                            translationY = offsetY
                        )
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(4.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Image Placeholder ${pageIndex + 1}\n(Pinch-to-Zoom enabled)\nScale: ${String.format("%.2f", scale)}\nOffset: (${String.format("%.0f", offsetX)}, ${String.format("%.0f", offsetY)})\nURI: $uriString",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        if (pagerState.pageCount > 1) {
            Row(
                Modifier
                    .height(20.dp) // Height for the row of indicators
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pagerState.pageCount) { iteration ->
                    val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(8.dp)
                    )
                }
            }
        }
    }
}

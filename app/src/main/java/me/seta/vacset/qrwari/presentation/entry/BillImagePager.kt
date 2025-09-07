package me.seta.vacset.qrwari.presentation.entry

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
// import androidx.compose.foundation.shape.RoundedCornerShape // No longer needed here for image clipping
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip // Still needed for dot indicators
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BillImagePager(
    imageUris: List<String>,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { imageUris.size })
    val scope = rememberCoroutineScope()

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) { // Outer box to contain pager and arrows
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = false, // Disable swipe navigation
                modifier = Modifier.fillMaxSize() // Pager fills the Box
            ) { pageIndex ->
                val uriString = imageUris[pageIndex]

                var scale by remember(uriString) { mutableStateOf(1f) }
                var offsetX by remember(uriString) { mutableStateOf(0f) }
                var offsetY by remember(uriString) { mutableStateOf(0f) }

                Box( // This Box is for gesture detection for the image
                    modifier = Modifier
                        .fillMaxSize()
                        // .clip(RoundedCornerShape(8.dp)) // Removed internal clip
                        .pointerInput(uriString) {
                            detectTransformGestures { centroid, pan, zoom, rotation ->
                                scale = (scale * zoom).coerceIn(1f, 3f)

                                if (scale > 1f) {
                                    offsetX += pan.x * scale
                                    offsetY += pan.y * scale
                                    // TODO: Add logic here to constrain offsetX and offsetY
                                } else {
                                    offsetX = 0f
                                    offsetY = 0f
                                }
                            }
                        }
                ) {
                    AsyncImage(
                        model = Uri.parse(uriString),
                        contentDescription = "Bill image ${pageIndex + 1}",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offsetX,
                                translationY = offsetY
                            )
                    )
                }
            }

            // Previous Button
            if (pagerState.pageCount > 1 && pagerState.currentPage > 0) {
                IconButton(
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(4.dp) // Reduced padding
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous image",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Next Button
            if (pagerState.pageCount > 1 && pagerState.currentPage < pagerState.pageCount - 1) {
                IconButton(
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(4.dp) // Reduced padding
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next image",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Pager dots (Indicator)
        if (pagerState.pageCount > 1) {
            Row(
                Modifier
                    .height(20.dp) // Height for the row of indicators
                    .fillMaxWidth()
                    .padding(top = 4.dp), // Reduced padding
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

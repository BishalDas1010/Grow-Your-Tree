package com.example.disipline.UI.onboarding

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.disipline.R
import kotlinx.coroutines.launch

// ---------- Colors ----------
private val Cream = Color(0xFFF6F2EB)
private val DarkGreen = Color(0xFF1F3A2E)
private val MidGreen = Color(0xFF4F8A5B)
private val CardGreenTop = Color(0xFFE3EFDD)
private val CardGreenBottom = Color(0xFFCFE3C9)
private val SubtitleGray = Color(0xFF8A9A90)
private val DotInactive = Color(0xFFCFCFC8)

// ---------- Page model ----------
private data class OnboardingPage(
    val title: String,
    val subtitle: String,
    @DrawableRes val image: Int
)

private val pages = listOf(
    OnboardingPage(
        title = "Grow Daily",
        subtitle = "Small sessions become\nstrong habits.",
        image = R.drawable.clamboy
    ),
    OnboardingPage(
        title = "Stay Focused",
        subtitle = "Plant a tree, avoid distractions\nand watch your forest grow.",
        image = R.drawable.floatingisland
    ),
    OnboardingPage(
        title = "Track Progress",
        subtitle = "See your streaks and build\na better you every day.",
        image = R.drawable.dreamygraan
    )
)

// ---------- Screen ----------
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    initialPage: Int = 0
) {
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val isLast = pagerState.currentPage == pages.lastIndex

    Scaffold(
        containerColor = Cream,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { index ->
                val page = pages[index]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 24.dp)
                ) {
                    // Illustration card: shorter fixed height
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(450.dp)
                            .width(200.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(Brush.verticalGradient(listOf(CardGreenTop, CardGreenBottom)))
                    ) {
                        Image(
                            painter = painterResource(page.image),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(Modifier.height(28.dp))

                    Text(
                        text = page.title,
                        color = DarkGreen,
                        fontSize = 50.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = page.subtitle,
                        color = SubtitleGray,
                        fontSize = 18.sp,
                        lineHeight = 20.sp
                    )
                    Spacer(Modifier.height(20.dp))
                }
            }

            // ---- Bottom bar: dots + Skip (left), arrow button (right) ----
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 24.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        repeat(pages.size) { i ->
                            val selected = i == pagerState.currentPage
                            Box(
                                modifier = Modifier
                                    .size(if (selected) 10.dp else 8.dp)
                                    .clip(CircleShape)
                                    .background(if (selected) MidGreen else DotInactive)
                            )
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                    Text(
                        text = "Skip",
                        color = SubtitleGray,
                        fontSize = 15.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onFinish)
                            .padding(vertical = 4.dp, horizontal = 2.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(DarkGreen)
                        .clickable {
                            if (!isLast) {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            } else onFinish()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.left),
                        contentDescription = if (isLast) "Finish" else "Next",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}

// ---------- Previews (must be top-level, outside OnboardingScreen) ----------
@Preview(showBackground = true, widthDp = 390, heightDp = 800, name = "Page 1")
@Composable
private fun Preview1() = OnboardingScreen(onFinish = {}, initialPage = 0)

@Preview(showBackground = true, widthDp = 390, heightDp = 800, name = "Page 2")
@Composable
private fun Preview2() = OnboardingScreen(onFinish = {}, initialPage = 1)

@Preview(showBackground = true, widthDp = 390, heightDp = 800, name = "Page 3")
@Composable
private fun Preview3() = OnboardingScreen(onFinish = {}, initialPage = 2)
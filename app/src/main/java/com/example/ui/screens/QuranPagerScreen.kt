package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.JuzMetadata
import com.example.data.local.QuranPage
import com.example.data.local.QuranPageAyah
import com.example.data.local.QuranPageRepository
import com.example.data.local.QuranSurahCatalog
import com.example.data.local.SurahMetadata
import com.example.ui.theme.AmiriQuranFont
import com.example.ui.theme.AtharDeepGreen
import com.example.ui.theme.AtharGlassBorder
import com.example.ui.theme.AtharGlassDarkGreen
import com.example.ui.theme.AtharGlassSecondary
import com.example.ui.theme.AtharGlassShadow
import com.example.ui.theme.AtharGold
import com.example.ui.theme.AtharGoldDark
import com.example.ui.theme.AtharIvory
import com.example.ui.theme.AtharIvoryDark
import com.example.ui.theme.CairoFontFamily
import com.example.ui.viewmodel.AtharViewModel
import kotlinx.coroutines.launch

@Composable
fun QuranPagerScreen(
    viewModel: AtharViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // 100% offline bundled 604 pages loaded once and cached in memory
    val pages = remember { QuranPageRepository.getPages(context) }
    val initialPage = remember {
        val lastRead = viewModel.getLastReadPage()
        if (lastRead in 1..604) lastRead - 1 else 0
    }

    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { 604 }
    )

    var showControls by remember { mutableStateOf(true) }
    var isIvoryPaperMode by remember { mutableStateOf(true) }
    var showSurahIndexDialog by remember { mutableStateOf(false) }
    var showJuzIndexDialog by remember { mutableStateOf(false) }
    var showJumpPageDialog by remember { mutableStateOf(false) }

    var bookmarkPage by remember { mutableIntStateOf(viewModel.getQuranBookmarkPage()) }
    val currentPageNumber = pagerState.currentPage + 1

    // Persist last read page as user swipes
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { index ->
            viewModel.setLastReadPage(index + 1)
        }
    }

    // Color tokens for Paper Mode vs Night Mode
    val pageBgColor = if (isIvoryPaperMode) Color(0xFFF9F5EC) else Color(0xFF0C1F1A)
    val pageTextColor = if (isIvoryPaperMode) Color(0xFF142E25) else Color(0xFFF5F0E6)
    val frameBorderColor = if (isIvoryPaperMode) AtharGold.copy(alpha = 0.55f) else AtharGold.copy(alpha = 0.40f)
    val headerFooterTextColor = if (isIvoryPaperMode) Color(0xFF755B23) else AtharGold

    // Guaranteed RTL reading direction for authentic Arabic Mushaf pagination
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(pageBgColor)
                .testTag("quran_pager_screen")
        ) {
            // Main 604-page Horizontal Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        showControls = !showControls
                    }
            ) { pageIndex ->
                val pageData = if (pageIndex < pages.size) pages[pageIndex] else null
                MedinaMushafPageView(
                    pageNumber = pageIndex + 1,
                    pageData = pageData,
                    textColor = pageTextColor,
                    frameBorderColor = frameBorderColor,
                    headerFooterTextColor = headerFooterTextColor,
                    isIvoryMode = isIvoryPaperMode
                )
            }

            // Top Floating Control Bar
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn() + slideInVertically { -it },
                exit = fadeOut() + slideOutVertically { -it },
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                MushafTopBar(
                    currentPage = currentPageNumber,
                    bookmarkPage = bookmarkPage,
                    isIvoryMode = isIvoryPaperMode,
                    onNavigateBack = onNavigateBack,
                    onToggleMode = { isIvoryPaperMode = !isIvoryPaperMode },
                    onBookmarkClicked = {
                        val newBookmark = currentPageNumber
                        bookmarkPage = newBookmark
                        viewModel.setQuranBookmarkPage(newBookmark)
                        Toast.makeText(
                            context,
                            "تم حفظ الفاصلة عند الصفحة ${toArabicDigits(newBookmark)}",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    onJumpToBookmark = {
                        coroutineScope.launch {
                            val target = (bookmarkPage - 1).coerceIn(0, 603)
                            pagerState.animateScrollToPage(target)
                        }
                    }
                )
            }

            // Bottom Floating Quick Access & Jump Bar
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it },
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                MushafBottomBar(
                    currentPage = currentPageNumber,
                    bookmarkPage = bookmarkPage,
                    onOpenSurahIndex = { showSurahIndexDialog = true },
                    onOpenJuzIndex = { showJuzIndexDialog = true },
                    onOpenJumpDialog = { showJumpPageDialog = true },
                    onSliderChanged = { targetPage ->
                        coroutineScope.launch {
                            pagerState.scrollToPage((targetPage - 1).coerceIn(0, 603))
                        }
                    }
                )
            }

            // 114 Surahs Index Dialog
            if (showSurahIndexDialog) {
                SurahIndexPickerDialog(
                    onDismiss = { showSurahIndexDialog = false },
                    onSurahSelected = { surah ->
                        showSurahIndexDialog = false
                        coroutineScope.launch {
                            pagerState.scrollToPage((surah.startPage - 1).coerceIn(0, 603))
                        }
                    }
                )
            }

            // 30 Juzs Index Dialog
            if (showJuzIndexDialog) {
                JuzIndexPickerDialog(
                    onDismiss = { showJuzIndexDialog = false },
                    onJuzSelected = { juz ->
                        showJuzIndexDialog = false
                        coroutineScope.launch {
                            pagerState.scrollToPage((juz.startPage - 1).coerceIn(0, 603))
                        }
                    }
                )
            }

            // Jump to Specific Page Dialog
            if (showJumpPageDialog) {
                JumpToPageDialog(
                    currentPage = currentPageNumber,
                    onDismiss = { showJumpPageDialog = false },
                    onPageSelected = { page ->
                        showJumpPageDialog = false
                        coroutineScope.launch {
                            pagerState.scrollToPage((page - 1).coerceIn(0, 603))
                        }
                    }
                )
            }
        }
    }
}

/**
 * Authentic Medina Mushaf Page Layout (framed with header, footer, Uthmani scripture, and Surah decorations)
 */
@Composable
private fun MedinaMushafPageView(
    pageNumber: Int,
    pageData: QuranPage?,
    textColor: Color,
    frameBorderColor: Color,
    headerFooterTextColor: Color,
    isIvoryMode: Boolean
) {
    val surahMeta = QuranSurahCatalog.getSurahByPage(pageNumber)
    val juzMeta = QuranSurahCatalog.getJuzByPage(pageNumber)

    // Accurate Hizb index (60 Hizbs across 604 pages)
    val hizbNumber = minOf(60, maxOf(1, ((pageNumber - 1) * 60 / 604) + 1))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 60.dp),
        contentAlignment = Alignment.Center
    ) {
        // Decorative Outer Mushaf Border
        Column(
            modifier = Modifier
                .fillMaxSize()
                .border(1.5.dp, frameBorderColor, RoundedCornerShape(18.dp))
                .padding(3.dp)
                .border(0.8.dp, frameBorderColor.copy(alpha = 0.5f), RoundedCornerShape(15.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            // --- TOP HEADER ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Surah Name (Right in RTL)
                Text(
                    text = "سورة ${surahMeta.arabicName}",
                    style = TextStyle(
                        fontFamily = AmiriQuranFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = headerFooterTextColor
                    )
                )

                // Diamond ornament
                Text(
                    text = "۞",
                    style = TextStyle(
                        fontFamily = AmiriQuranFont,
                        fontSize = 14.sp,
                        color = AtharGold
                    )
                )

                // Juz Title (Left in RTL)
                Text(
                    text = juzMeta.arabicName.substringBefore(" -"),
                    style = TextStyle(
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = headerFooterTextColor
                    )
                )
            }

            // Divider under header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                frameBorderColor,
                                AtharGold,
                                frameBorderColor,
                                Color.Transparent
                            )
                        )
                    )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // --- PAGE BODY (Uthmani Scripture & Surah Banners) ---
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (pageData == null || pageData.ayahs.isEmpty()) {
                    // Fallback preview while loading
                    Text(
                        text = "جاري فتح صفحة ${toArabicDigits(pageNumber)} من مصحف المدينة...",
                        style = TextStyle(
                            fontFamily = CairoFontFamily,
                            fontSize = 14.sp,
                            color = textColor.copy(alpha = 0.7f)
                        )
                    )
                } else {
                    RenderMushafPageContent(
                        pageNumber = pageNumber,
                        ayahs = pageData.ayahs,
                        textColor = textColor,
                        isIvoryMode = isIvoryMode
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Divider above footer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                frameBorderColor,
                                AtharGold,
                                frameBorderColor,
                                Color.Transparent
                            )
                        )
                    )
            )

            // --- BOTTOM FOOTER ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hizb
                Text(
                    text = "الحزب ${toArabicDigits(hizbNumber)}",
                    style = TextStyle(
                        fontFamily = CairoFontFamily,
                        fontSize = 12.sp,
                        color = headerFooterTextColor
                    )
                )

                // Page Number in Ornamental Medallion
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(if (isIvoryMode) Color(0x33D6BC82) else Color(0x33244638))
                        .border(1.dp, AtharGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = toArabicDigits(pageNumber),
                        style = TextStyle(
                            fontFamily = AmiriQuranFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = headerFooterTextColor
                        )
                    )
                }

                // Reference Label
                Text(
                    text = "مصحف المدينة",
                    style = TextStyle(
                        fontFamily = CairoFontFamily,
                        fontSize = 11.5.sp,
                        color = headerFooterTextColor.copy(alpha = 0.85f)
                    )
                )
            }
        }
    }
}

/**
 * Renders Surah opening banners, Basmala, and continuous Uthmani verses for the page
 */
@Composable
private fun RenderMushafPageContent(
    pageNumber: Int,
    ayahs: List<QuranPageAyah>,
    textColor: Color,
    isIvoryMode: Boolean
) {
    var i = 0
    while (i < ayahs.size) {
        val ayah = ayahs[i]

        if (ayah.isNewSurah) {
            val surah = QuranSurahCatalog.getSurahByNumber(ayah.surah)
            if (surah != null) {
                SurahHeaderBanner(
                    surah = surah,
                    isIvoryMode = isIvoryMode
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Basmala (Every Surah except Surah 9 At-Tawbah and Surah 1 where it's ayah 1)
                if (surah.number != 1 && surah.number != 9) {
                    BasmalaBanner(isIvoryMode = isIvoryMode)
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }

        // Group consecutive ayahs of the same Surah block into a paragraph
        val currentSurah = ayah.surah
        val blockAyahs = mutableListOf<QuranPageAyah>()
        while (i < ayahs.size && ayahs[i].surah == currentSurah && (blockAyahs.isEmpty() || !ayahs[i].isNewSurah)) {
            blockAyahs.add(ayahs[i])
            i++
        }

        val formattedText = buildString {
            blockAyahs.forEach { item ->
                var text = item.text.trim()
                // If Surah > 1 and starts with Basmala, strip Basmala from verse 1 text since banner displayed it
                if (item.surah != 1 && item.ayah == 1 && text.startsWith("بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ")) {
                    text = text.removePrefix("بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ").trim()
                }
                append(text)
                append(" ﴿")
                append(toArabicDigits(item.ayah))
                append("﴾ ")
            }
        }

        Text(
            text = formattedText,
            style = TextStyle(
                fontFamily = AmiriQuranFont,
                fontWeight = FontWeight.Normal,
                fontSize = if (pageNumber <= 2) 23.sp else 20.sp,
                lineHeight = if (pageNumber <= 2) 44.sp else 38.sp,
                textAlign = TextAlign.Center,
                color = textColor
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))
    }
}

/**
 * Ornamental Surah Title Banner (برواز اسم السورة)
 */
@Composable
private fun SurahHeaderBanner(
    surah: SurahMetadata,
    isIvoryMode: Boolean
) {
    val bg = if (isIvoryMode) Color(0x33D6BC82) else Color(0x33102D25)
    val borderCol = AtharGold

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.2.dp, borderCol, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${surah.versesCount} آيات",
                style = TextStyle(
                    fontFamily = CairoFontFamily,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AtharGold
                )
            )

            Text(
                text = "سُورَةُ ${surah.arabicName}",
                style = TextStyle(
                    fontFamily = AmiriQuranFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = AtharGold
                )
            )

            Text(
                text = surah.revelationType,
                style = TextStyle(
                    fontFamily = CairoFontFamily,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AtharGold
                )
            )
        }
    }
}

/**
 * Ornamental Calligraphic Basmala Banner
 */
@Composable
private fun BasmalaBanner(isIvoryMode: Boolean) {
    Text(
        text = "بِسۡمِ ٱللَّهِ ٱلرَّحۡمَٰنِ ٱلرَّحِيمِ",
        style = TextStyle(
            fontFamily = AmiriQuranFont,
            fontWeight = FontWeight.Bold,
            fontSize = 21.sp,
            color = AtharGoldDark,
            textAlign = TextAlign.Center
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    )
}

/**
 * Top floating toolbar with back button, bookmark action, and mode toggle
 */
@Composable
private fun MushafTopBar(
    currentPage: Int,
    bookmarkPage: Int,
    isIvoryMode: Boolean,
    onNavigateBack: () -> Unit,
    onToggleMode: () -> Unit,
    onBookmarkClicked: () -> Unit,
    onJumpToBookmark: () -> Unit
) {
    val surah = QuranSurahCatalog.getSurahByPage(currentPage)
    val hasBookmarkHere = currentPage == bookmarkPage

    Surface(
        color = Color(0xF0102D25),
        border = BorderStroke(1.dp, AtharGlassBorder),
        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "العودة",
                        tint = AtharIvory
                    )
                }

                Column(modifier = Modifier.padding(start = 4.dp)) {
                    Text(
                        text = "سورة ${surah.arabicName} • صفحة ${toArabicDigits(currentPage)}",
                        style = TextStyle(
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.5.sp,
                            color = AtharGold
                        )
                    )
                    Text(
                        text = "${surah.revelationType} • الجزء ${toArabicDigits(surah.juzNumber)}",
                        style = TextStyle(
                            fontFamily = CairoFontFamily,
                            fontSize = 11.5.sp,
                            color = AtharIvoryDark
                        )
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Reading Mode Toggle (Ivory Paper vs Dark Night)
                IconButton(onClick = onToggleMode) {
                    Icon(
                        imageVector = if (isIvoryMode) Icons.Filled.DarkMode else Icons.Filled.LightMode,
                        contentDescription = "تبديل المظهر",
                        tint = AtharGold
                    )
                }

                // Bookmark current page
                IconButton(onClick = onBookmarkClicked) {
                    Icon(
                        imageVector = if (hasBookmarkHere) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                        contentDescription = "حفظ الفاصلة",
                        tint = if (hasBookmarkHere) AtharGold else AtharIvory
                    )
                }

                // Quick jump to bookmark if elsewhere
                if (!hasBookmarkHere) {
                    TextButton(onClick = onJumpToBookmark) {
                        Text(
                            text = "الفاصلة (${toArabicDigits(bookmarkPage)})",
                            style = TextStyle(
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                color = AtharGold
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Bottom floating toolbar with Surah picker, Juz picker, page slider, and direct jump
 */
@Composable
private fun MushafBottomBar(
    currentPage: Int,
    bookmarkPage: Int,
    onOpenSurahIndex: () -> Unit,
    onOpenJuzIndex: () -> Unit,
    onOpenJumpDialog: () -> Unit,
    onSliderChanged: (Int) -> Unit
) {
    Surface(
        color = Color(0xF2102D25),
        border = BorderStroke(1.dp, AtharGlassBorder),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        shadowElevation = 12.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Slider Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "١",
                    style = TextStyle(
                        fontFamily = CairoFontFamily,
                        fontSize = 11.sp,
                        color = AtharIvoryDark
                    )
                )

                Slider(
                    value = currentPage.toFloat(),
                    onValueChange = { onSliderChanged(it.toInt()) },
                    valueRange = 1f..604f,
                    colors = SliderDefaults.colors(
                        thumbColor = AtharGold,
                        activeTrackColor = AtharGold,
                        inactiveTrackColor = AtharGlassSecondary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                )

                Text(
                    text = "٦٠٤",
                    style = TextStyle(
                        fontFamily = CairoFontFamily,
                        fontSize = 11.sp,
                        color = AtharIvoryDark
                    )
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Action Buttons Row: فهرس السور (114) • الأجزاء (30) • انتقال لصفحة
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onOpenSurahIndex,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AtharGlassSecondary,
                        contentColor = AtharIvory
                    ),
                    border = BorderStroke(1.dp, AtharGlassBorder),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1.1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.FormatListNumbered,
                        contentDescription = null,
                        tint = AtharGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "السور (١١٤)",
                        style = TextStyle(
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    )
                }

                Button(
                    onClick = onOpenJuzIndex,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AtharGlassSecondary,
                        contentColor = AtharIvory
                    ),
                    border = BorderStroke(1.dp, AtharGlassBorder),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        tint = AtharGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "الأجزاء (٣٠)",
                        style = TextStyle(
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    )
                }

                Button(
                    onClick = onOpenJumpDialog,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AtharGold,
                        contentColor = AtharDeepGreen
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Navigation,
                        contentDescription = null,
                        tint = AtharDeepGreen,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ص: ${toArabicDigits(currentPage)}",
                        style = TextStyle(
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }
    }
}

/**
 * 114 Surahs Picker Dialog with search, verse count, and Medina start page
 */
@Composable
private fun SurahIndexPickerDialog(
    onDismiss: () -> Unit,
    onSurahSelected: (SurahMetadata) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = remember(searchQuery) {
        if (searchQuery.isBlank()) QuranSurahCatalog.surahs else {
            QuranSurahCatalog.surahs.filter {
                it.arabicName.contains(searchQuery, ignoreCase = true) ||
                        "${it.number}".contains(searchQuery) ||
                        it.revelationType.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = AtharDeepGreen),
            border = BorderStroke(1.2.dp, AtharGlassBorder),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 580.dp)
                .shadow(16.dp, RoundedCornerShape(24.dp), spotColor = AtharGlassShadow)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "فهرس سور القرآن الكريم (١١٤ سورة)",
                        style = TextStyle(
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = AtharGold
                        )
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(imageVector = Icons.Filled.Clear, contentDescription = "إغلاق", tint = AtharIvory)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "ابحث بالاسم أو رقم السورة...",
                            style = TextStyle(fontFamily = CairoFontFamily, fontSize = 13.sp)
                        )
                    },
                    leadingIcon = {
                        Icon(imageVector = Icons.Filled.Search, contentDescription = null, tint = AtharGold)
                    },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AtharGold,
                        unfocusedBorderColor = AtharGlassBorder,
                        focusedContainerColor = AtharGlassDarkGreen,
                        unfocusedContainerColor = AtharGlassDarkGreen,
                        focusedTextColor = AtharIvory,
                        unfocusedTextColor = AtharIvory
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtered, key = { it.number }) { surah ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AtharGlassSecondary,
                            border = BorderStroke(0.8.dp, AtharGlassBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSurahSelected(surah) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Surah number pill
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Color(0x33D6BC82))
                                            .border(1.dp, AtharGold, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${surah.number}",
                                            style = TextStyle(
                                                fontFamily = CairoFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = AtharGold
                                            )
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column {
                                        Text(
                                            text = "سورة ${surah.arabicName}",
                                            style = TextStyle(
                                                fontFamily = AmiriQuranFont,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 17.sp,
                                                color = AtharIvory
                                            )
                                        )
                                        Text(
                                            text = "${surah.revelationType} • ${surah.versesCount} آيات • الجزء ${toArabicDigits(surah.juzNumber)}",
                                            style = TextStyle(
                                                fontFamily = CairoFontFamily,
                                                fontSize = 11.5.sp,
                                                color = AtharIvoryDark
                                            )
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = AtharGold.copy(alpha = 0.15f),
                                    border = BorderStroke(0.5.dp, AtharGold)
                                ) {
                                    Text(
                                        text = "ص ${toArabicDigits(surah.startPage)}",
                                        style = TextStyle(
                                            fontFamily = CairoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.5.sp,
                                            color = AtharGold
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 30 Juzs Picker Dialog with Medina start page
 */
@Composable
private fun JuzIndexPickerDialog(
    onDismiss: () -> Unit,
    onJuzSelected: (JuzMetadata) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = AtharDeepGreen),
            border = BorderStroke(1.2.dp, AtharGlassBorder),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 560.dp)
                .shadow(16.dp, RoundedCornerShape(24.dp), spotColor = AtharGlassShadow)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "أجزاء القرآن الكريم (٣٠ جزءاً)",
                        style = TextStyle(
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = AtharGold
                        )
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(imageVector = Icons.Filled.Clear, contentDescription = "إغلاق", tint = AtharIvory)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(QuranSurahCatalog.juzs, key = { it.number }) { juz ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AtharGlassSecondary,
                            border = BorderStroke(0.8.dp, AtharGlassBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onJuzSelected(juz) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 11.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(Color(0x33D6BC82))
                                            .border(1.dp, AtharGold, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = toArabicDigits(juz.number),
                                            style = TextStyle(
                                                fontFamily = AmiriQuranFont,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = AtharGold
                                            )
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = juz.arabicName,
                                            style = TextStyle(
                                                fontFamily = CairoFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = AtharIvory
                                            )
                                        )
                                        Text(
                                            text = "بداية من سورة ${juz.startSurahName}",
                                            style = TextStyle(
                                                fontFamily = CairoFontFamily,
                                                fontSize = 11.5.sp,
                                                color = AtharIvoryDark
                                            )
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = AtharGold.copy(alpha = 0.15f),
                                    border = BorderStroke(0.5.dp, AtharGold)
                                ) {
                                    Text(
                                        text = "ص ${toArabicDigits(juz.startPage)}",
                                        style = TextStyle(
                                            fontFamily = CairoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = AtharGold
                                        ),
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Direct Jump to Page Dialog (1..604)
 */
@Composable
private fun JumpToPageDialog(
    currentPage: Int,
    onDismiss: () -> Unit,
    onPageSelected: (Int) -> Unit
) {
    var pageInputText by remember { mutableStateOf("$currentPage") }
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = AtharDeepGreen),
            border = BorderStroke(1.2.dp, AtharGlassBorder),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(16.dp, RoundedCornerShape(22.dp))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "انتقال إلى صفحة في مصحف المدينة",
                    style = TextStyle(
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = AtharGold
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "أدخل رقم الصفحة من ١ إلى ٦٠٤",
                    style = TextStyle(
                        fontFamily = CairoFontFamily,
                        fontSize = 12.sp,
                        color = AtharIvoryDark
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = pageInputText,
                    onValueChange = { pageInputText = it },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            val num = pageInputText.toIntOrNull()
                            if (num != null && num in 1..604) {
                                onPageSelected(num)
                            } else {
                                Toast.makeText(context, "الرجاء إدخال رقم بين 1 و 604", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ),
                    singleLine = true,
                    textStyle = TextStyle(
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center,
                        color = AtharIvory
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AtharGold,
                        unfocusedBorderColor = AtharGlassBorder,
                        focusedContainerColor = AtharGlassDarkGreen,
                        unfocusedContainerColor = AtharGlassDarkGreen
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.width(140.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, AtharGlassBorder),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "إلغاء",
                            style = TextStyle(
                                fontFamily = CairoFontFamily,
                                color = AtharIvoryDark
                            )
                        )
                    }

                    Button(
                        onClick = {
                            val num = pageInputText.toIntOrNull()
                            if (num != null && num in 1..604) {
                                onPageSelected(num)
                            } else {
                                Toast.makeText(context, "الرجاء إدخال رقم بين 1 و 604", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AtharGold,
                            contentColor = AtharDeepGreen
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "انتقال",
                            style = TextStyle(
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Utility to convert Hindu-Arabic digits (0..9) to Eastern Arabic Numerals (٠..٩)
 */
fun toArabicDigits(number: Int): String {
    val arabicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    val str = number.toString()
    val sb = StringBuilder(str.length)
    for (ch in str) {
        if (ch in '0'..'9') {
            sb.append(arabicDigits[ch - '0'])
        } else {
            sb.append(ch)
        }
    }
    return sb.toString()
}

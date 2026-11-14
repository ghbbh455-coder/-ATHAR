package com.example.ui.components

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.toArabicDigits
import com.example.ui.theme.AmiriQuranFont
import com.example.ui.theme.AtharDeepGreen
import com.example.ui.theme.AtharGlassBorder
import com.example.ui.theme.AtharGlassBorderBrush
import com.example.ui.theme.AtharGlassDarkGreen
import com.example.ui.theme.AtharGlassSecondary
import com.example.ui.theme.AtharGold
import com.example.ui.theme.AtharGoldDark
import com.example.ui.theme.AtharGoldLight
import com.example.ui.theme.AtharIvory
import com.example.ui.theme.AtharIvoryDark
import com.example.ui.theme.AtharSecondaryGreen
import com.example.ui.theme.LiquidGlassDefaults
import com.example.ui.theme.rememberLiquidGlassBrush
import com.example.util.ShareImageHelper
import kotlinx.coroutines.launch

enum class SpiritualMood(
    val title: String,
    val subtitle: String,
    val emoji: String
) {
    DISTRESSED("مهموم أو متضايق", "سكينة وتفريج كرب", "🌧️"),
    CONFUSED("محتار في أمر", "استخارة وهداية وتوكل", "🧭"),
    REPENTANT("مقصّر وأريد العودة", "رحمة ومغفرة وأمل", "🕊️"),
    GRATEFUL("ممتن وسعيد", "شكر وحمد واستزادة", "🌸")
}

@Immutable
data class MoodGuidanceItem(
    val id: String,
    val badge: String,
    val mainText: String,
    val reference: String,
    val reflection: String
)

object MoodGuidanceCatalog {
    val guidanceMap: Map<SpiritualMood, List<MoodGuidanceItem>> = mapOf(
        SpiritualMood.DISTRESSED to listOf(
            MoodGuidanceItem(
                id = "mood_dist_1",
                badge = "آية كريمة • طمأنينة وسكينة",
                mainText = "الَّذِينَ آمَنُوا وَتَطْمَئِنُّ قُلُوبُهُم بِذِكْرِ اللَّهِ ۗ أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ",
                reference = "سورة الرعد: ٢٨",
                reflection = "يا صاحب الهمّ.. مهما تعاظمت عليك الهموم، فذكر الله واحة أمان تتساقط عندها كل أثقال الحياة."
            ),
            MoodGuidanceItem(
                id = "mood_dist_2",
                badge = "آية كريمة • وعد بالفرج",
                mainText = "لَا تَدْرِي لَعَلَّ اللَّهَ يُحْدِثُ بَعْدَ ذَٰلِكَ أَمْرًا",
                reference = "سورة الطلاق: ١",
                reflection = "ما من ضيق إلا وخلفه فرج يدبره الله لك في خفاء لطفه؛ فنم قرير العين واستبشر."
            ),
            MoodGuidanceItem(
                id = "mood_dist_3",
                badge = "دعاء نبوي • تفريج الهم والغم",
                mainText = "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْهَمِّ وَالْحَزَنِ ، وَالْعَجْزِ وَالْكَسَلِ ، وَالْبُخْلِ وَالْجُبْنِ ، وَضَلَعِ الدَّيْنِ وَغَلَبَةِ الرِّجَالِ",
                reference = "صحيح البخاري، برقم 2893",
                reflection = "كان رسول الله ﷺ يكثر من هذه الاستعاذة؛ فاستند بها إلى كفاية الله وقوته."
            ),
            MoodGuidanceItem(
                id = "mood_dist_4",
                badge = "آية كريمة • دعاء يونس عليه السلام",
                mainText = "فَنَادَىٰ فِي الظُّلُمَاتِ أَن لَّا إِلَٰهَ إِلَّا أَنتَ سُبْحَانَكَ إِنِّي كُنتُ مِنَ الظَّالِمِينَ ۝ فَاسْتَجَبْنَا لَهُ وَنَجَّيْنَاهُ مِنَ الْغَمِّ ۚ وَكَذَٰلِكَ نُنجِي الْمُؤْمِنِينَ",
                reference = "سورة الأنبياء: ٨٧ - ٨٨",
                reflection = "لم يدعُ بها مكروب إلا كشف الله كربه وأبدله فرجاً وسلاماً."
            )
        ),
        SpiritualMood.CONFUSED to listOf(
            MoodGuidanceItem(
                id = "mood_conf_1",
                badge = "آية كريمة • التوكل والكفاية",
                mainText = "وَمَن يَتَوَكَّلْ عَلَى اللَّهِ فَهُوَ حَسْبُهُ ۚ إِنَّ اللَّهَ بَالِغُ أَمْرِهِ",
                reference = "سورة الطلاق: ٣",
                reflection = "حين تتحير في اختيارك، سلّم دفة أمورك لله وتوكل عليه؛ فحسبك به وكيلاً وهادياً."
            ),
            MoodGuidanceItem(
                id = "mood_conf_2",
                badge = "دعاء الاستخارة النبوية",
                mainText = "اللَّهُمَّ إِنِّي أَسْتَخِيرُكَ بِعِلْمِكَ ، وَأَسْتَقْدِرُكَ بِقُدْرَتِكَ ، وَأَسْأَلُكَ مِنْ فَضْلِكَ الْعَظِيمِ ، فَإِنَّكَ تَقْدِرُ وَلَا أَقْدِرُ ، وَتَعْلَمُ وَلَا أَعْلَمُ ، وَأَنْتَ عَلَّامُ الْغُيُوبِ",
                reference = "صحيح البخاري، برقم 1166",
                reflection = "صلّ ركعتين ثم ادع بهذا الدعاء بقلبٍ مفوّض؛ فإن الله سيختار لك ما فيه خير دينك ودنياك."
            ),
            MoodGuidanceItem(
                id = "mood_conf_3",
                badge = "دعاء الهداية والسداد",
                mainText = "اللَّهُمَّ اهْدِنِي وَسَدِّدْنِي ، وَاذْكُرْ بِالْهُدَى هِدَايَتَكَ الطَّرِيقَ ، وَالسَّدَادِ سَدَادَ السَّهْمِ",
                reference = "صحيح مسلم، برقم 2725",
                reflection = "سؤال الهداية والسداد يفتح بصيرتك للحق ويجنبك عواقب الحيرة والتردد."
            ),
            MoodGuidanceItem(
                id = "mood_conf_4",
                badge = "آية كريمة • الاستعانة بالله",
                mainText = "وَتَوَكَّلْ عَلَى الْحَيِّ الَّذِي لَا يَمُوتُ وَسَبِّحْ بِحَمْدِهِ",
                reference = "سورة الفرقان: ٥٨",
                reflection = "الخلق يغيبون ويضعفون، والله الحي القيوم لا يغيب ولا يعجزه شيء؛ فاستند إلى جنابه."
            )
        ),
        SpiritualMood.REPENTANT to listOf(
            MoodGuidanceItem(
                id = "mood_rep_1",
                badge = "آية كريمة • نداء الرجاء والمغفرة",
                mainText = "قُلْ يَا عِبَادِيَ الَّذِينَ أَسْرَفُوا عَلَىٰ أَنفُسِهِمْ لَا تَقْنَطُوا مِن رَّحْمَةِ اللَّهِ ۚ إِنَّ اللَّهَ يَغْفِرُ الذُّنُوبَ جَمِيعًا ۚ إِنَّهُ هُوَ الْغَفُورُ الرَّحِيمُ",
                reference = "سورة الزمر: ٥٣",
                reflection = "مهما بعدت أو تكرر منك الخطأ؛ رحمة الله تنتظرك وباب التوبة مشرع لا يوصد أبداً."
            ),
            MoodGuidanceItem(
                id = "mood_rep_2",
                badge = "آية كريمة • فضل التوبة الصادقة",
                mainText = "وَإِنِّي لَغَفَّارٌ لِّمَن تَابَ وَآمَنَ وَعَمِلَ صَالِحًا ثُمَّ اهْتَدَىٰ",
                reference = "سورة طه: ٨٢",
                reflection = "خطوة صادقة واحدة نحو الله تكفيك لتبدأ صفحة بيضاء نقية مع رب كريم."
            ),
            MoodGuidanceItem(
                id = "mood_rep_3",
                badge = "دعاء آدم وحواء عليهما السلام",
                mainText = "رَبَّنَا ظَلَمْنَا أَنفُسَنَا وَإِن لَّمْ تَغْفِرْ لَنَا وَتَرْحَمْنَا لَنَكُونَنَّ مِنَ الْخَاسِرِينَ",
                reference = "سورة الأعراف: ٢٣",
                reflection = "الاعتراف بالتقصير بين يدي الله هو بوابة الفتح والقبول والرحمة الربانية."
            ),
            MoodGuidanceItem(
                id = "mood_rep_4",
                badge = "الاستغفار الماحي للذنوب",
                mainText = "أَسْتَغْفِرُ اللَّهَ الْعَظِيمَ الَّذِي لَا إِلَهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ وَأَتُوبُ إِلَيْهِ",
                reference = "سنن الترمذي (3577) وأبو داود",
                reflection = "من قالها ثلاثاً غُفرت ذنوبه وإن كانت مثل زبد البحر."
            )
        ),
        SpiritualMood.GRATEFUL to listOf(
            MoodGuidanceItem(
                id = "mood_grat_1",
                badge = "آية كريمة • وعد الزيادة بالشكر",
                mainText = "وَإِذْ تَأَذَّنَ رَبُّكُمْ لَئِن شَكَرْتُمْ لَأَزِيدَنَّكُمْ",
                reference = "سورة إبراهيم: ٧",
                reflection = "الشكر قيد النعم ومفتاح المزيد؛ فكلما ازداد لسانك حمداً، تدفقت عليك بركات الله."
            ),
            MoodGuidanceItem(
                id = "mood_grat_2",
                badge = "آية كريمة • الذكر والشكر",
                mainText = "فَاذْكُرُونِي أَذْكُرْكُمْ وَاشْكُرُوا لِي وَلَا تَكْفُرُونِ",
                reference = "سورة البقرة: ١٥٢",
                reflection = "ما أعظم هذا الفضل! تذكر الله في أرضه فيذكرك سبحانه في ملأ خير من ملئك."
            ),
            MoodGuidanceItem(
                id = "mood_grat_3",
                badge = "دعاء سليمان عليه السلام",
                mainText = "رَبِّ أَوْزِعْنِي أَنْ أَشْكُرَ نِعْمَتَكَ الَّتِي أَنْعَمْتَ عَلَيَّ وَعَلَىٰ وَالِدَيَّ وَأَنْ أَعْمَلَ صَالِحًا تَرْضَاهُ",
                reference = "سورة النمل: ١٩",
                reflection = "اسأل الله دائماً أن يوفقك لشكره، فالشكر نفسه نعمة عظيمة تستوجب الحمد."
            ),
            MoodGuidanceItem(
                id = "mood_grat_4",
                badge = "دعاء النبي ﷺ عند الفرح والسرور",
                mainText = "الْحَمْدُ لِلَّهِ الَّذِي بِنِعْمَتِهِ تَتِمُّ الصَّالِحَاتُ",
                reference = "سنن ابن ماجه، برقم 3803",
                reflection = "كان ﷺ إذا رأى ما يحب قال هذا الدعاء متأدباً ومقرّاً بأن كل خير هو بفضل الله وحده."
            )
        )
    )
}

/**
 * Interactive Liquid Glass Card displayed on the Home Screen inviting the user
 * to select their current emotional / spiritual state.
 */
@Composable
fun MoodSelectorCard(
    onSelectMood: (SpiritualMood) -> Unit,
    modifier: Modifier = Modifier
) {
    val glassBrush = rememberLiquidGlassBrush()
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, LiquidGlassDefaults.ThinGoldBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("mood_selector_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = glassBrush)
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0x33D6BC82),
                        border = BorderStroke(1.dp, AtharGold),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = "🤍", fontSize = 16.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "بماذا تشعر الآن؟",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = AtharGold
                            )
                        )
                        Text(
                            text = "اختر ما يمر به قلبك لتجد نوراً وسكينة",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = AtharIvoryDark,
                                fontSize = 11.5.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2x2 Grid of Mood Chips
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MoodButton(
                        mood = SpiritualMood.DISTRESSED,
                        onClick = { onSelectMood(SpiritualMood.DISTRESSED) },
                        modifier = Modifier.weight(1f)
                    )
                    MoodButton(
                        mood = SpiritualMood.CONFUSED,
                        onClick = { onSelectMood(SpiritualMood.CONFUSED) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MoodButton(
                        mood = SpiritualMood.REPENTANT,
                        onClick = { onSelectMood(SpiritualMood.REPENTANT) },
                        modifier = Modifier.weight(1f)
                    )
                    MoodButton(
                        mood = SpiritualMood.GRATEFUL,
                        onClick = { onSelectMood(SpiritualMood.GRATEFUL) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun MoodButton(
    mood: SpiritualMood,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0x2E102D25),
        border = BorderStroke(1.dp, AtharGlassBorder),
        modifier = modifier
            .wrapContentHeight()
            .clickable(onClick = onClick)
            .testTag("mood_btn_${mood.name.lowercase()}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = mood.emoji, fontSize = 18.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = mood.title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = AtharGold
                    )
                )
                Text(
                    text = mood.subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = AtharIvoryDark,
                        fontSize = 10.5.sp
                    ),
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Liquid Glass ModalBottomSheet presenting curated scriptures & supplications
 * corresponding to the chosen mood, with cycling, copy and image-sharing actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodGuidanceSheet(
    mood: SpiritualMood,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onCopy: (String) -> Unit,
    onShare: (String, String) -> Unit,
    fontSizeMultiplier: Float = 1.0f
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val items = remember(mood) { MoodGuidanceCatalog.guidanceMap[mood] ?: emptyList() }
    var currentIndex by remember(mood) { mutableIntStateOf(0) }

    val currentItem = if (items.isNotEmpty()) items[currentIndex.coerceIn(0, items.lastIndex)] else null

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AtharDeepGreen,
        dragHandle = {
            Surface(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .size(width = 44.dp, height = 4.dp),
                shape = CircleShape,
                color = AtharGold.copy(alpha = 0.5f)
            ) {}
        },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .testTag("mood_guidance_sheet")
        ) {
            // Top Bar with Mood info & Dismiss Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = mood.emoji, fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = mood.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = AtharGold
                            )
                        )
                        Text(
                            text = mood.subtitle,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = AtharIvoryDark,
                                fontSize = 11.5.sp
                            )
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "إغلاق",
                        tint = AtharIvory
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (currentItem != null) {
                AnimatedContent(
                    targetState = currentItem,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "mood_content_crossfade"
                ) { item ->
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = AtharGlassSecondary),
                        border = BorderStroke(1.dp, LiquidGlassDefaults.ThinGoldBorderColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            // Badge
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = AtharGold.copy(alpha = 0.18f),
                                border = BorderStroke(0.8.dp, AtharGold)
                            ) {
                                Text(
                                    text = item.badge,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = AtharGold,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Main Scripture
                            Text(
                                text = "«${item.mainText}»",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontFamily = AmiriQuranFont,
                                    fontSize = (21 * fontSizeMultiplier).sp,
                                    lineHeight = (32 * fontSizeMultiplier).sp,
                                    color = AtharGoldLight,
                                    textAlign = TextAlign.Center
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Reference
                            Text(
                                text = item.reference,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = AtharIvoryDark,
                                    textAlign = TextAlign.Center
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Reflection Snippet
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0x35102D25),
                                border = BorderStroke(0.8.dp, AtharGold.copy(alpha = 0.25f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "💡 ${item.reflection}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = AtharIvory,
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp
                                    ),
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Action Row (Cycle, Share as Image, Copy, Share Text)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Next / Cycle Button
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AtharGold.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, AtharGold),
                        modifier = Modifier
                            .clickable {
                                if (items.isNotEmpty()) {
                                    currentIndex = (currentIndex + 1) % items.size
                                }
                            }
                            .testTag("mood_cycle_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = null,
                                tint = AtharGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "تذكير آخر (${toArabicDigits(currentIndex + 1)}/${toArabicDigits(items.size)})",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = AtharGold,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    // Share & Copy Actions
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Image Share Button
                        Surface(
                            shape = CircleShape,
                            color = AtharGold,
                            modifier = Modifier
                                .size(40.dp)
                                .clickable {
                                    coroutineScope.launch {
                                        ShareImageHelper.shareCardAsImage(
                                            context = context,
                                            badgeText = currentItem.badge,
                                            mainText = currentItem.mainText,
                                            referenceText = currentItem.reference,
                                            benefitOrReflection = currentItem.reflection
                                        )
                                    }
                                }
                                .testTag("mood_share_image_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Image,
                                    contentDescription = "مشاركة كصورة",
                                    tint = AtharDeepGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Copy Button
                        Surface(
                            shape = CircleShape,
                            color = AtharGlassSecondary,
                            border = BorderStroke(1.dp, AtharGlassBorder),
                            modifier = Modifier
                                .size(40.dp)
                                .clickable {
                                    onCopy("${currentItem.mainText}\n[${currentItem.reference}]\n${currentItem.reflection}")
                                }
                                .testTag("mood_copy_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.ContentCopy,
                                    contentDescription = "نسخ",
                                    tint = AtharIvory,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Text Share Button
                        Surface(
                            shape = CircleShape,
                            color = AtharGlassSecondary,
                            border = BorderStroke(1.dp, AtharGlassBorder),
                            modifier = Modifier
                                .size(40.dp)
                                .clickable {
                                    onShare(currentItem.mainText, currentItem.reference)
                                }
                                .testTag("mood_share_text_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Share,
                                    contentDescription = "مشاركة",
                                    tint = AtharIvory,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

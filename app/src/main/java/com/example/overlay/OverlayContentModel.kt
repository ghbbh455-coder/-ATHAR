package com.example.overlay

import androidx.compose.runtime.Immutable
import com.example.data.model.ContentType

@Immutable
data class OverlayContentItem(
    val id: String,
    val contentType: ContentType,
    val badgeText: String,
    val mainText: String,
    val referenceText: String,
    val topic: String,
    val benefitOrTafsir: String
)

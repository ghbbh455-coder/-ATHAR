package com.example.overlay

import com.example.data.model.ContentType

data class OverlayContentItem(
    val id: String,
    val contentType: ContentType,
    val badgeText: String,
    val mainText: String,
    val referenceText: String,
    val topic: String,
    val benefitOrTafsir: String
)

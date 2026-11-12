package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object ShareImageHelper {

    suspend fun shareCardAsImage(
        context: Context,
        badgeText: String,
        mainText: String,
        referenceText: String = "",
        benefitOrReflection: String = ""
    ) {
        withContext(Dispatchers.IO) {
            try {
                val bitmap = createAtharCardBitmap(
                    context = context,
                    badgeText = badgeText,
                    mainText = mainText,
                    referenceText = referenceText,
                    benefitOrReflection = benefitOrReflection
                )

                // Save bitmap to cache directory
                val cacheFolder = File(context.cacheDir, "shared_images").apply {
                    if (!exists()) mkdirs()
                }
                val imageFile = File(cacheFolder, "athar_${System.currentTimeMillis()}.png")
                FileOutputStream(imageFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }

                // Get content URI via FileProvider
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    imageFile
                )

                withContext(Dispatchers.Main) {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/png"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        putExtra(Intent.EXTRA_TEXT, "«$mainText»\n\nمن تطبيق أَثَــر | «افتح هاتفك على ما ينفع قلبك»")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }

                    val chooser = Intent.createChooser(shareIntent, "مشاركة البطاقة الإيمانية").apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(chooser)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "حدث خطأ أثناء إعداد الصورة للمشاركة", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun createAtharCardBitmap(
        context: Context,
        badgeText: String,
        mainText: String,
        referenceText: String,
        benefitOrReflection: String
    ): Bitmap {
        val width = 1080
        val height = 1350
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Background Gradient (Dark Green Glass: #102D25 to #1B392F)
        val bgPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                Color.parseColor("#102D25"),
                Color.parseColor("#1B392F"),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // 2. Outer Decorative Gold Border
        val borderMargin = 40f
        val borderRadius = 44f
        val borderPaint = Paint().apply {
            color = Color.parseColor("#D6BC82")
            style = Paint.Style.STROKE
            strokeWidth = 5f
            isAntiAlias = true
        }
        val borderRect = RectF(borderMargin, borderMargin, width - borderMargin, height - borderMargin)
        canvas.drawRoundRect(borderRect, borderRadius, borderRadius, borderPaint)

        // Inner Subtle Border
        val innerMargin = borderMargin + 14f
        val innerPaint = Paint().apply {
            color = Color.parseColor("#50D6BC82")
            style = Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        }
        val innerRect = RectF(innerMargin, innerMargin, width - innerMargin, height - innerMargin)
        canvas.drawRoundRect(innerRect, borderRadius - 8f, borderRadius - 8f, innerPaint)

        // 3. Top Header: App Branding & Badge
        val topContentY = 110f

        // App Title
        val brandPaint = Paint().apply {
            color = Color.parseColor("#D6BC82")
            textSize = 42f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("أَثَــر • ATHAR", width / 2f, topContentY, brandPaint)

        // Category Badge Pill
        val cleanBadge = badgeText.ifBlank { "تذكير إيماني" }
        val badgeTextPaint = Paint().apply {
            color = Color.parseColor("#F5F0E6")
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val badgeWidth = badgeTextPaint.measureText(cleanBadge) + 60f
        val badgeHeight = 56f
        val badgeY = topContentY + 45f
        val badgeRect = RectF(
            (width - badgeWidth) / 2f,
            badgeY,
            (width + badgeWidth) / 2f,
            badgeY + badgeHeight
        )
        val badgeBgPaint = Paint().apply {
            color = Color.parseColor("#33F5F0E6")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawRoundRect(badgeRect, 28f, 28f, badgeBgPaint)
        canvas.drawRoundRect(badgeRect, 28f, 28f, borderPaint.apply { strokeWidth = 2f })
        canvas.drawText(cleanBadge, width / 2f, badgeY + 38f, badgeTextPaint)

        // Decorative separator line under header
        val sepPaint = Paint().apply {
            color = Color.parseColor("#40D6BC82")
            strokeWidth = 2.5f
            isAntiAlias = true
        }
        val lineY = badgeY + badgeHeight + 50f
        canvas.drawLine(160f, lineY, width - 160f, lineY, sepPaint)

        // 4. Main Scripture / Text Layout
        val textPaint = TextPaint().apply {
            color = Color.parseColor("#F5F0E6")
            textSize = 46f
            typeface = Typeface.create("serif", Typeface.BOLD)
            isAntiAlias = true
        }

        val contentWidth = width - 200
        val staticLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(mainText, 0, mainText.length, textPaint, contentWidth)
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setLineSpacing(22f, 1.25f)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(
                mainText,
                textPaint,
                contentWidth,
                Layout.Alignment.ALIGN_CENTER,
                1.25f,
                22f,
                false
            )
        }

        // Center the main text vertically in the available middle space
        val availableMiddleHeight = height - lineY - 350f
        val textStartY = lineY + (availableMiddleHeight - staticLayout.height) / 2f.coerceAtLeast(40f)

        canvas.save()
        canvas.translate(100f, textStartY)
        staticLayout.draw(canvas)
        canvas.restore()

        // 5. Reference & Benefit / Reflection Section
        val footerSectionY = textStartY + staticLayout.height + 60f

        // Separator diamond
        val diamondPaint = Paint().apply {
            color = Color.parseColor("#D6BC82")
            textSize = 30f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("۞", width / 2f, footerSectionY, diamondPaint)

        // Subtext / Reflection
        val subtextCombined = buildString {
            if (referenceText.isNotBlank()) append(referenceText)
            if (benefitOrReflection.isNotBlank()) {
                if (isNotEmpty()) append(" • ")
                append(benefitOrReflection)
            }
        }.ifBlank { "«افتح هاتفك على ما ينفع قلبك»" }

        val subtextPaint = TextPaint().apply {
            color = Color.parseColor("#D6BC82")
            textSize = 30f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val subtextLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(subtextCombined, 0, subtextCombined.length, subtextPaint, contentWidth)
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setLineSpacing(14f, 1.2f)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(
                subtextCombined,
                subtextPaint,
                contentWidth,
                Layout.Alignment.ALIGN_CENTER,
                1.2f,
                14f,
                false
            )
        }

        canvas.save()
        canvas.translate(100f, footerSectionY + 40f)
        subtextLayout.draw(canvas)
        canvas.restore()

        // 6. Bottom Gift Signature & App Watermark
        val giftSignaturePaint = Paint().apply {
            color = Color.parseColor("#D6BC82")
            textSize = 26f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("«أُهديت إليك من قلبٍ يحب لك الخير.. عبر تطبيق أثر»", width / 2f, height - 90f, giftSignaturePaint)

        val watermarkPaint = Paint().apply {
            color = Color.parseColor("#80F5F0E6")
            textSize = 21f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("أَثَــر • «افتح هاتفك على ما ينفع قلبك»", width / 2f, height - 50f, watermarkPaint)

        return bitmap
    }
}

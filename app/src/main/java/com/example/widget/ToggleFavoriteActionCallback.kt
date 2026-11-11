package com.example.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.updateAll
import com.example.data.local.AtharPreferences
import com.example.data.local.db.AtharDatabase
import com.example.data.local.db.FavoriteEntity
import java.util.UUID

class ToggleFavoriteActionCallback : ActionCallback {

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        try {
            val prefs = AtharPreferences(context)
            val currentContent = prefs.getWidgetContent()

            val db = AtharDatabase.getDatabase(context)
            val isCurrentlyFav = db.favoriteDao().isFavoriteSync(currentContent.id)
            val newFavState = !isCurrentlyFav

            if (newFavState) {
                db.favoriteDao().insertFavorite(
                    FavoriteEntity(
                        id = UUID.randomUUID().toString(),
                        contentId = currentContent.id,
                        contentType = currentContent.type.uppercase(),
                        createdAt = System.currentTimeMillis()
                    )
                )
                prefs.toggleFavorite(currentContent.id)
            } else {
                db.favoriteDao().deleteFavoriteByContentId(currentContent.id)
                prefs.toggleFavorite(currentContent.id)
            }

            prefs.saveWidgetContent(
                currentContent.copy(isFavorite = newFavState)
            )

            AtharGlanceWidget().update(context, glanceId)
            AtharGlanceWidget().updateAll(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

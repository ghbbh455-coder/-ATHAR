package com.example.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.updateAll
import com.example.data.local.AtharPreferences

class ReloadContentActionCallback : ActionCallback {

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        try {
            val prefs = AtharPreferences(context)
            val nextItem = AtharContentSelector.getNextRandomItem(context)

            prefs.saveWidgetContent(
                AtharPreferences.WidgetContent(
                    id = nextItem.id,
                    type = nextItem.targetDestination,
                    badge = nextItem.badge,
                    text = nextItem.text,
                    reference = nextItem.reference,
                    isFavorite = nextItem.isFavorite
                )
            )

            AtharGlanceWidget().update(context, glanceId)
            AtharGlanceWidget().updateAll(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

package dev.tabhishekpaul.notification_listener

import android.app.Notification
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import dev.tabhishekpaul.notification_listener.models.Action

object NotificationUtils {

    private val REPLY_KEYWORDS = arrayOf("reply", "android.intent.extra.text")
    private const val INPUT_KEYWORD = "input"

    fun getBitmapFromDrawable(drawable: Drawable): Bitmap {
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }


    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun getQuickReplyAction(notification: Notification, packageName: String): Action? {
        val action = getNotificationAction(notification)
            ?: getWearReplyAction(notification)
        return action?.let { Action(it, packageName, true) }
    }

    private fun getNotificationAction(notification: Notification): NotificationCompat.Action? {
        for (i in 0 until NotificationCompat.getActionCount(notification)) {
            val action = NotificationCompat.getAction(notification, i)
            action?.remoteInputs?.forEach { input ->
                if (isKnownReplyKey(input.resultKey)) return action
            }
        }
        return null
    }

    private fun getWearReplyAction(notification: Notification): NotificationCompat.Action? {
        val wearableExtender = NotificationCompat.WearableExtender(notification)
        wearableExtender.actions.forEach { action ->
            action.remoteInputs?.forEach { input ->
                if (isKnownReplyKey(input.resultKey) || input.resultKey.contains(INPUT_KEYWORD, true)) {
                    return action
                }
            }
        }
        return null
    }

    private fun isKnownReplyKey(resultKey: String?): Boolean {
        if (resultKey.isNullOrEmpty()) return false
        return REPLY_KEYWORDS.any { resultKey.contains(it, ignoreCase = true) }
    }
}

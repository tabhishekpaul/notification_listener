package dev.tabhishekpaul.notification_listener

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap

import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.annotation.RequiresApi
import dev.tabhishekpaul.notification_listener.models.ActionCache
import java.io.ByteArrayOutputStream

@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class NotificationListener : NotificationListenerService() {

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        handleNotification(sbn, isRemoved = false)
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        handleNotification(sbn, isRemoved = true)
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun handleNotification(notification: StatusBarNotification, isRemoved: Boolean) {
        try{
            val packageName = notification.packageName
            val extras = notification.notification.extras
            val appIcon = getAppIcon(packageName)
            val largeIcon = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getNotificationLargeIcon(applicationContext, notification.notification)
            } else null

            val action = NotificationUtils.getQuickReplyAction(notification.notification, packageName)

            val intent = Intent(NotificationConstants.INTENT).apply {
                putExtra(NotificationConstants.PACKAGE_NAME, packageName)
                putExtra(NotificationConstants.ID, notification.id)
                putExtra(NotificationConstants.CAN_REPLY, action != null)

                // Cache quick-reply actions
                action?.let { ActionCache.cachedNotifications[notification.id] = it }

                putExtra(NotificationConstants.NOTIFICATIONS_ICON, appIcon)
                putExtra(NotificationConstants.LARGE_ICON, largeIcon)

                extras?.let {
                    val title = it.getCharSequence(Notification.EXTRA_TITLE)?.toString()
                    val text = it.getCharSequence(Notification.EXTRA_TEXT)?.toString()

                    putExtra(NotificationConstants.NOTIFICATION_TITLE, title)
                    putExtra(NotificationConstants.NOTIFICATION_CONTENT, text)
                    putExtra(NotificationConstants.IS_REMOVED, isRemoved)
                    putExtra(NotificationConstants.HAVE_EXTRA_PICTURE, it.containsKey(Notification.EXTRA_PICTURE))

                    if (it.containsKey(Notification.EXTRA_PICTURE)) {
                        val bmp = it.get(Notification.EXTRA_PICTURE) as Bitmap
                        val stream = ByteArrayOutputStream()
                        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream)
                        putExtra(NotificationConstants.EXTRAS_PICTURE, stream.toByteArray())
                    }
                }
            }
            sendBroadcast(intent)
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    private fun getAppIcon(packageName: String): ByteArray? {
        return try {
            val manager = baseContext.packageManager
            val icon = manager.getApplicationIcon(packageName)
            val stream = ByteArrayOutputStream()
            NotificationUtils.getBitmapFromDrawable(icon).compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.toByteArray()
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            null
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getNotificationLargeIcon(context: Context, notification: Notification): ByteArray? {
        return try {
            val bitmap = notification.largeIcon
            bitmap?.let {
                val outputStream = ByteArrayOutputStream()
                it.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.toByteArray()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

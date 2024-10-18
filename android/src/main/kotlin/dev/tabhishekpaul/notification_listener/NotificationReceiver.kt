package dev.tabhishekpaul.notification_listener

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build.VERSION_CODES
import androidx.annotation.RequiresApi
import io.flutter.plugin.common.EventChannel.EventSink

class NotificationReceiver(private val eventSink: EventSink?) : BroadcastReceiver() {

    @RequiresApi(api = VERSION_CODES.JELLY_BEAN_MR2)
    override fun onReceive(context: Context?, intent: Intent?) {
       try {
           val packageName = intent?.getStringExtra(NotificationConstants.PACKAGE_NAME)
           val title = intent?.getStringExtra(NotificationConstants.NOTIFICATION_TITLE)
           val content = intent?.getStringExtra(NotificationConstants.NOTIFICATION_CONTENT)
           val notificationIcon = intent?.getByteArrayExtra(NotificationConstants.NOTIFICATIONS_ICON)
           val notificationExtrasPicture = intent?.getByteArrayExtra(NotificationConstants.EXTRAS_PICTURE)
           val largeIcon = intent?.getByteArrayExtra(NotificationConstants.LARGE_ICON)
           val haveExtraPicture = intent?.getBooleanExtra(NotificationConstants.HAVE_EXTRA_PICTURE, false) ?: false
           val hasRemoved = intent?.getBooleanExtra(NotificationConstants.IS_REMOVED, false) ?: false
           val canReply = intent?.getBooleanExtra(NotificationConstants.CAN_REPLY, false) ?: false
           val id = intent?.getIntExtra(NotificationConstants.ID, -1) ?: -1

           val data = hashMapOf<String, Any?>(
               "id" to id,
               "packageName" to packageName,
               "title" to title,
               "content" to content,
               "notificationIcon" to notificationIcon,
               "notificationExtrasPicture" to notificationExtrasPicture,
               "haveExtraPicture" to haveExtraPicture,
               "largeIcon" to largeIcon,
               "hasRemoved" to hasRemoved,
               "canReply" to canReply
           )

           eventSink?.success(data)
       }catch (e:Exception){
           e.printStackTrace()
       }
    }
}

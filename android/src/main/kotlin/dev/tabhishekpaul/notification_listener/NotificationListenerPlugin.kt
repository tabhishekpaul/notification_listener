package dev.tabhishekpaul.notification_listener

import androidx.annotation.NonNull

import io.flutter.embedding.engine.plugins.FlutterPlugin

import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding

import android.content.ComponentName
import android.provider.Settings
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.app.Activity

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.IntentFilter
import android.os.Build

import dev.tabhishekpaul.notification_listener.models.ActionCache;

/** NotificationListenerPlugin */
class NotificationListenerPlugin: FlutterPlugin, ActivityAware, PluginRegistry.ActivityResultListener,MethodCallHandler,EventChannel.StreamHandler {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  
  private val eventTag: String = "notification_listener_event_channel"
  private val channelTag: String = "notification_listener_channel"
  private var notificationReceiver: NotificationReceiver? = null
  private lateinit var eventChannel: EventChannel
  private lateinit var channel : MethodChannel
  private var mainActivity: Activity? = null
  private lateinit var pluginResult: Result
  private lateinit var context: Context
  private val requestCode = 189
  

  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    context = flutterPluginBinding.applicationContext
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, channelTag)
    channel.setMethodCallHandler(this)
    eventChannel = EventChannel(flutterPluginBinding.binaryMessenger, eventTag)
    eventChannel.setStreamHandler(this)
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
   try{
       if (call.method == "request") {
           if(mainActivity != null) {
               val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
               mainActivity?.startActivityForResult(intent, requestCode)
           } else {
               result.error("NO_ACTIVITY","No Activity context found.",null)
           }
       }  else if (call.method.equals("isGranted")) {
           result.success(isGranted(context))
       }  else if (call.method == "sendReply") {
           val message: String? = call.argument("message")
           val notificationId: Int? = call.argument("notificationId")

           if (notificationId != null && message != null) {
               val action = ActionCache.cachedNotifications[notificationId]
               if (action == null) {
                   result.error("Notification", "Can't find this cached notification", null)
               } else {
                   try {
                       action.sendReply(context, message)
                       result.success(true)
                   } catch (e: PendingIntent.CanceledException) {
                       result.success(false)
                       e.printStackTrace()
                   }
               }
           } else {
               result.error("Invalid Arguments", "Notification ID or message is null", null)
           }
       } else {
           result.notImplemented()
       }
   }catch (_:Exception){}
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    try {
      channel.setMethodCallHandler(null)
      eventChannel.setStreamHandler(null)
    }catch (_:Exception){}
  }

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    mainActivity = binding.activity
  }

  override fun onDetachedFromActivityForConfigChanges() {
    mainActivity = null
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    onAttachedToActivity(binding)
  }

  override fun onDetachedFromActivity() {
    mainActivity = null
  }


  override fun onActivityResult(code: Int, resultCode: Int, data: Intent?): Boolean {
    try {
      if (requestCode == code) {
        when (resultCode) {
          Activity.RESULT_OK -> pluginResult.success(true)
          Activity.RESULT_CANCELED -> {
            val isEnabled = isGranted(context)
            pluginResult.success(isEnabled)
          }
          else -> pluginResult.success(false)
        }
        return true
      }
    }catch (_:Exception){}
     return false
   }

  private fun isGranted(context: Context): Boolean {
    val packageName = context.packageName
    val flat = Settings.Secure.getString(
        context.contentResolver, "enabled_notification_listeners"
    ) ?: return false

    if (!flat.isNullOrEmpty()) {
        val names = flat.split(":")
        for (name in names) {
            val componentName = ComponentName.unflattenFromString(name)
            if (componentName != null && TextUtils.equals(packageName, componentName.packageName)) {
                return true
            }
        }
    }
    return false
  }

  @SuppressLint("WrongConstant")
  override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
      val intentFilter = IntentFilter().apply {
          addAction(NotificationConstants.INTENT)
      }
      notificationReceiver = NotificationReceiver(events)

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
          context.registerReceiver(notificationReceiver, intentFilter, Context.RECEIVER_EXPORTED)
      } else {
          context.registerReceiver(notificationReceiver, intentFilter)
      }

      val listenerIntent = Intent(context, NotificationReceiver::class.java)
      context.startService(listenerIntent)
  }

  override fun onCancel(arguments: Any?) {
      context.unregisterReceiver(notificationReceiver)
      notificationReceiver = null
  }
}

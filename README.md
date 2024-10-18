# **notification_listener**

A **Flutter plugin** to integrate with Android's `NotificationListenerService`, enabling apps to detect, log, and respond to notifications. This plugin provides a way to listen to new notifications, handle their removal, and reply directly when supported.

---

## **Installation and Setup**

1. Add the plugin dependency in your `pubspec.yaml`:

```yaml
dependencies:
  notification_listener: any # Replace 'any' with the latest version.
```

2. Add the following service declaration to your **`AndroidManifest.xml`** to bind the notification service with your application:

```xml
        <service android:label="notifications" android:name="dev.tabhishekpaul.notification_listener.NotificationListener"
                android:permission="android.permission.BIND_NOTIFICATION_LISTENER_SERVICE" android:exported="true">
            <intent-filter>
                <action android:name="android.service.notification.NotificationListenerService" />
            </intent-filter>
        </service>
```

3. Request **notification access** from the user through **Settings**:
   - **Settings** → **Apps & notifications** → **Special app access** → **Notification access**.

---

## **Usage Example**

### 1. **Checking Notification Permission**

Check if the app has notification access:

```dart
final bool isGranted = await AndroidNotificationListener.isGranted();
if (isGranted) {
  print('Notification access granted!');
} else {
  print('Notification access denied.');
}
```

### 2. **Requesting Notification Permission**

Open the notification settings page and wait for the user to grant permission:

```dart
final bool granted = await AndroidNotificationListener.request();
if (granted) {
  print('Permission granted!');
}
```

### 3. **Listening for Notifications**

Stream incoming notification events:

```dart
AndroidNotificationListener.accessStream.listen((event) {
  print("Notification from ${event.packageName}: ${event.title}");
});
```

### 4. **Replying to Notifications**

Send a direct message reply to a notification:

```dart
try {
  await event.sendReply("This is an auto-response.");
} catch (e) {
  print('Error sending reply: $e');
}
```

The `ServiceNotificationEvent` provides:

- **id**: The notification ID.
- **canReply**: Whether the notification supports replies.
- **haveExtraPicture**: If the notification contains an image.
- **hasRemoved**: If the notification has been removed.
- **packageName**: The originating app's package name.
- **title**: The notification title.
- **content**: The main content of the notification.
- **appIcon / extrasPicture / largeIcon**: Available images for display.

---

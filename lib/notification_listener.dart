import 'package:notification_listener/models/notification.dart';
import 'package:flutter/services.dart';
import 'dart:developer';
import 'dart:async';
import 'dart:io';

class AndroidNotificationListener {
  AndroidNotificationListener._();

  static const MethodChannel _methodChannel =
      MethodChannel('notification_listener_channel');
  static const EventChannel _eventChannel =
      EventChannel('notification_listener_event_channel');
  static Stream<AndroidNotificationEvent>? _stream;

  /// stream the incoming Accessibility events
  static Stream<AndroidNotificationEvent> get accessStream {
    if (Platform.isAndroid) {
      _stream ??=
          _eventChannel.receiveBroadcastStream().map<AndroidNotificationEvent>(
                (event) => AndroidNotificationEvent.fromMap(event),
              );
      return _stream!;
    }
    throw Exception(
        "Notification Listener API exclusively available on Android!");
  }

  /// send a direct message reply to the incoming notification
  static Future<bool> sendReply(String message, int? id) async {
    try {
      return await _methodChannel.invokeMethod<bool>("sendReply", {
            'message': message,
            'notificationId': id,
          }) ??
          false;
    } catch (e) {
      rethrow;
    }
  }

  /// request accessibility permission
  /// it will open the accessibility settings page and return `true` once the permission granted.
  static Future<bool> request() async {
    try {
      return await _methodChannel.invokeMethod('request');
    } on PlatformException catch (error) {
      log("$error");
      return Future.value(false);
    }
  }

  /// check if accessibility permession is enebaled
  static Future<bool> isGranted() async {
    try {
      return await _methodChannel.invokeMethod('isGranted');
    } on PlatformException catch (error) {
      log("$error");
      return false;
    }
  }
}

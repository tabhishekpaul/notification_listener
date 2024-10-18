package dev.tabhishekpaul.notification_listener.models

import dev.tabhishekpaul.notification_listener.models.Action

object ActionCache {
    val cachedNotifications: MutableMap<Int, Action> = mutableMapOf()
}

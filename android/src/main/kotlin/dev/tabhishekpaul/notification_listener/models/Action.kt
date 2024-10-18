package dev.tabhishekpaul.notification_listener.models

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput


class Action : Parcelable {

    private val text: String
    private val packageName: String
    private val p: PendingIntent
    private val isQuickReply: Boolean
    private val remoteInputs = ArrayList<RemoteInputParcel>()

    constructor(parcel: Parcel) {
        text = parcel.readString() ?: ""
        packageName = parcel.readString() ?: ""
        p = parcel.readParcelable(PendingIntent::class.java.classLoader)
            ?: throw IllegalArgumentException("PendingIntent cannot be null")
        isQuickReply = parcel.readByte() != 0.toByte()
        parcel.readTypedList(remoteInputs, RemoteInputParcel.CREATOR)
    }

    constructor(
        text: String,
        packageName: String,
        p: PendingIntent,
        remoteInput: RemoteInput,
        isQuickReply: Boolean
    ) {
        this.text = text
        this.packageName = packageName
        this.p = p
        this.isQuickReply = isQuickReply
        remoteInputs.add(RemoteInputParcel(remoteInput))
    }

    constructor(action: NotificationCompat.Action, packageName: String, isQuickReply: Boolean) {
        this.text = action.title.toString()
        this.packageName = packageName
        this.p = action.actionIntent
            ?: throw IllegalArgumentException("PendingIntent cannot be null")
        action.remoteInputs?.forEach { remoteInputs.add(RemoteInputParcel(it)) }
        this.isQuickReply = isQuickReply
    }

    fun sendReply(context: Context, msg: String) {
        val intent = Intent()
        val bundle = Bundle()
        val actualInputs = ArrayList<RemoteInput>()

        for (input in remoteInputs) {
            val resultKey = input.getResultKey() ?: ""
            bundle.putCharSequence(resultKey, msg)
            val builder = RemoteInput.Builder(resultKey)
                .setLabel(input.getLabel() ?: "")
                .setChoices(input.getChoices())
                .setAllowFreeFormInput(input.isAllowFreeFormInput())
                .addExtras(input.getExtras() ?: Bundle()) // Handle nullable Bundle

            actualInputs.add(builder.build())
        }

        val inputs = actualInputs.toTypedArray()
        RemoteInput.addResultsToIntent(inputs, intent, bundle)
        p.send(context, 0, intent)
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(text)
        dest.writeString(packageName)
        dest.writeParcelable(p, flags)
        dest.writeByte(if (isQuickReply) 1 else 0)
        dest.writeTypedList(remoteInputs)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Action> {
        override fun createFromParcel(parcel: Parcel): Action = Action(parcel)
        override fun newArray(size: Int): Array<Action?> = arrayOfNulls(size)
    }
}

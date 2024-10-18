package dev.tabhishekpaul.notification_listener.models

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.core.app.RemoteInput

class RemoteInputParcel : Parcelable {

    private var label: String? = null
    private var resultKey: String? = null
    private var choices: Array<String> = arrayOf()
    private var allowFreeFormInput: Boolean = false
    private var extras: Bundle? = null

    constructor(input: RemoteInput) {
        label = input.label?.toString()
        resultKey = input.resultKey
        charSequenceToStringArray(input.choices)
        allowFreeFormInput = input.allowFreeFormInput
        extras = input.extras
    }

    constructor(parcel: Parcel) {
        label = parcel.readString()
        resultKey = parcel.readString()
        choices = parcel.createStringArray() ?: arrayOf()
        allowFreeFormInput = parcel.readByte() != 0.toByte()
        extras = parcel.readParcelable(Bundle::class.java.classLoader)
    }

    private fun charSequenceToStringArray(charSequence: Array<CharSequence>?) {
        if (charSequence != null) {
            choices = Array(charSequence.size) { i -> charSequence[i].toString() }
        }
    }

    fun getResultKey(): String? = resultKey
    fun getLabel(): String? = label
    fun getChoices(): Array<CharSequence> = choices as Array<CharSequence>
    fun isAllowFreeFormInput(): Boolean = allowFreeFormInput
    fun getExtras(): Bundle? = extras

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(label)
        dest.writeString(resultKey)
        dest.writeStringArray(choices)
        dest.writeByte(if (allowFreeFormInput) 1 else 0)
        dest.writeParcelable(extras, flags)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<RemoteInputParcel> {
        override fun createFromParcel(parcel: Parcel): RemoteInputParcel {
            return RemoteInputParcel(parcel)
        }

        override fun newArray(size: Int): Array<RemoteInputParcel?> {
            return arrayOfNulls(size)
        }
    }
}

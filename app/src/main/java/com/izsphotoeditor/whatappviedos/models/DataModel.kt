package com.izsphotoeditor.whatappviedos.models

import android.os.Parcelable
import android.os.Parcel
data class DataModel(val path:String, val name:String, val isVideo:Boolean, val videoSize : Long) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readByte() != 0.toByte(),
        parcel.readLong()
    )
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(path)
        parcel.writeString(name)
        parcel.writeByte(if (isVideo) 1 else 0)
        parcel.writeLong(videoSize)
    }
    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DataModel> {
        override fun createFromParcel(parcel: Parcel): DataModel {
            return DataModel(parcel)
        }

        override fun newArray(size: Int): Array<DataModel?> {
            return arrayOfNulls(size)
        }
    }
}

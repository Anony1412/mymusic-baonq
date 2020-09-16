package com.ptit.mymusic.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Song(val title: String, val uri: Uri): Parcelable

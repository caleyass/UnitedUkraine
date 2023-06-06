package com.example.un.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Charity(
    val user_id: String = "",
    val user_name: String = "",
    val title:String="",
    val goal: Long = 0,
    val description: String = "",
    val card: Long = 0,
    val image: String = "",
    val category: String = "",
    val checked: Boolean = false,
    var id: String = ""
    ) : Parcelable
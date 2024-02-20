package com.izsphotoeditor.whatappviedos.helpers

import android.view.View
import android.widget.ImageView
import android.widget.VideoView
import androidx.constraintlayout.widget.ConstraintLayout

interface VideoPlayCallback {
    fun getCallback(
        videoPath:String,
        videoView:VideoView,
        mainContainer: ConstraintLayout
    )
}
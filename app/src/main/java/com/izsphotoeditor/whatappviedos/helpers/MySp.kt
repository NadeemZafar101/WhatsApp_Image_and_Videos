package com.izsphotoeditor.whatappviedos.helpers

import android.content.Context

class MySp(val context: Context) {
    private val sharePreferences = context.getSharedPreferences("mySp", Context.MODE_PRIVATE)
    private val editor = sharePreferences.edit()

    fun setSp(uri: String){
        editor.putString("selectedUri",uri)
        editor.apply()
    }
    fun getSp(): String {
       return sharePreferences.getString("selectedUri","")!!
    }

}
package com.gi.hybridplayer.view

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.Window

class BaseDialog(context: Context) {
    private val dialog: Dialog
    private var positiveButton: View? = null
    private var negativeButton: View? = null

    init {
        dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    fun setContentView(layoutResource: Int){
        dialog.setContentView(layoutResource)
    }
    fun setPositiveButton(btnResource: Int, listener: View.OnClickListener){
        positiveButton = dialog.findViewById(btnResource)
        positiveButton?.setOnClickListener(listener)
    }
    fun setNegativeButton(btnResource: Int, listener: View.OnClickListener){
        negativeButton = dialog.findViewById(btnResource)
        negativeButton?.setOnClickListener(listener)
    }

    fun show(){
        dialog.show()
    }
    fun dismiss(){
        dialog.dismiss()
    }


}
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
    fun bindContentView(view: View){
        dialog.setContentView(view)
    }
    fun setWidth(width: Int){
        val params = dialog.window?.attributes
        params?.width = width
        dialog.window?.attributes = params
    }
    fun setHeight(height: Int){
        val params = dialog.window?.attributes
        params?.height = height
        dialog.window?.attributes = params
    }
    fun setPositiveButton(btnResource: Int, listener: View.OnClickListener){
        positiveButton = dialog.findViewById(btnResource)
        positiveButton?.setOnClickListener(listener)
    }
    fun setNegativeButton(btnResource: Int, listener: View.OnClickListener){
        negativeButton = dialog.findViewById(btnResource)
        negativeButton?.setOnClickListener(listener)
    }
    fun setTitle(title: String){
        dialog.setTitle(title)
    }

    fun show(){
        dialog.show()
    }
    fun dismiss(){
        dialog.dismiss()
    }


}
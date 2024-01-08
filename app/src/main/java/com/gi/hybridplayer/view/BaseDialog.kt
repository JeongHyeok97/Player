package com.gi.hybridplayer.view

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.KeyEvent
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.Window
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.gi.hybridplayer.R

open class BaseDialog(context: Context) {
    private val dialog: Dialog
    private var positiveButton: View? = null
    private var negativeButton: View? = null

    init {
        dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }
    fun setOnKeyListener(){
        val editText = dialog.findViewById<EditText>(R.id.search_input)
        val recyclerView = dialog.findViewById<RecyclerView>(R.id.search_result)
        dialog.setOnKeyListener(object : DialogInterface.OnKeyListener{
            override fun onKey(d: DialogInterface?, keyCode: Int, event: KeyEvent?): Boolean {
                if (keyCode == KeyEvent.KEYCODE_BACK){
                    return if (editText.hasFocus()){
                        false
                    } else{
                        editText.isFocusable = true
                        editText.requestFocus()
                        true
                    }
                }
                else if (keyCode == KeyEvent.KEYCODE_DEL){
                    if (!editText.hasFocus()){

                    }
                }
                else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && !editText.hasFocus()){
                    editText.isFocusable = false
                }
                else if (keyCode == KeyEvent.KEYCODE_DPAD_UP){
                    editText.isFocusable = true
                }
                else if (keyCode == KeyEvent.KEYCODE_PROG_RED || keyCode == KeyEvent.KEYCODE_PROG_GREEN){
                    val adapter = recyclerView.adapter as SearchResultAdapter
                    val sortType = when (keyCode) {
                        KeyEvent.KEYCODE_PROG_RED -> SearchResultAdapter.SORT_BY_NAME
                        KeyEvent.KEYCODE_PROG_GREEN -> SearchResultAdapter.SORT_BY_NUMBER
                        else -> -1
                    }
                    adapter.sort(sortType)
                    if (adapter.itemCount > 0) {
                        recyclerView.scrollToPosition(0)
                    }
                }
                return false
            }
        })
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
    fun setCancelListener(listener: DialogInterface.OnDismissListener){
        dialog.setOnDismissListener(listener)
    }

    fun show(){
        dialog.show()
    }
    fun dismiss(){
        dialog.dismiss()
    }

    open fun getSetupDialog(): Dialog {
        return dialog
    }


}
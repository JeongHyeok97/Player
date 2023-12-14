package com.gi.hybridplayer.view

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.Window
import android.widget.EditText
import android.widget.Toast
import com.gi.hybridplayer.R

class TextMatchDialog(context: Context, resultListener: OnSuccessListener) {

    private val dialog: Dialog
    private var inputTextView: EditText? = null
    private var targetText: String? = ""
    private val mContext:Context
    private val mListener:OnSuccessListener

    init {
        mContext = context
        mListener = resultListener
        dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    interface OnSuccessListener{
        fun onSuccess()
    }

    fun setContentView(resource: Int){
        dialog.setContentView(resource)
        val params = dialog.window?.attributes
        params?.width = mContext.resources.getDimensionPixelSize(R.dimen.text_dialog_width)
        params?.height = mContext.resources.getDimensionPixelSize(R.dimen.text_dialog_height)
        dialog.window?.attributes = params
    }

    fun setMainText(resource: Int){
        inputTextView = dialog.findViewById(resource)
    }


    fun setTargetText(password: String?){
        this.targetText = password
        inputTextView?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.length == 4){
                    if (s.toString() == password){
                        mListener.onSuccess()
                        dialog.cancel()
                    }
                    else{
                        Toast.makeText(mContext, "PIN code mismatch", Toast.LENGTH_SHORT).show()
                        inputTextView?.setText("")
                        inputTextView?.requestFocus()
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {
            }
        })
    }

    fun show(){
        dialog.show()
    }


}
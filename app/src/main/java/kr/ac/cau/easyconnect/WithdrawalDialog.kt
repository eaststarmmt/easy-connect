package kr.ac.cau.easyconnect

import android.app.Dialog
import android.content.Context
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText

class WithdrawalDialog(context: Context) {
    ////////////////////////////////////////////////////// 회원 탈퇴 문구 띄우려는 dialog 구현중인데 쓸지는 고민중입니다. + (withdrawal_dialog.xml) 2021-04-15 04:39
    private val dialog = Dialog(context)

    fun myDiag(){
        val edit_content = dialog.findViewById<EditText>(R.id.content)
        val button_ok = dialog.findViewById<Button>(R.id.ok)
        val button_cancel = dialog.findViewById<Button>(R.id.cancel)

        dialog.setContentView(R.layout.withdrawal_dialog)

        dialog.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)

        dialog.show()

        button_ok.setOnClickListener{
            onClickListener.onClicked(edit_content.text.toString())
            dialog.dismiss()
        }

        button_cancel.setOnClickListener{
            dialog.dismiss()
        }
    }

    interface ButtonClickListener{
        fun onClicked(password: String)
    }

    private lateinit var onClickListener: ButtonClickListener

    fun setOnClickedListener(listener: ButtonClickListener){
        onClickListener = listener
    }
}
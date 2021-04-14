package kr.ac.cau.easyconnect

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Debug
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.auth.User
import com.google.firebase.ktx.Firebase

class Page_find_information : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page_find_information)

        val firebaseAuth : FirebaseAuth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        val button_find_id : Button = findViewById(R.id.bt_find_id)
        val button_find_password : Button = findViewById(R.id.bt_find_password)
        val editText_check_phonenumber_fi : EditText = findViewById(R.id.edit_check_phonenumber_fi)
        val editText_check_phonenumber2_fi : EditText = findViewById(R.id.edit_check_phonenumber2_fi)
        val editText_check_id_fi : EditText = findViewById(R.id.edit_check_id_fi)
        val editText_check_name_fi : EditText = findViewById(R.id.edit_check_name_fi)

        var user_name : String
        var user_email : String

        button_find_id.setOnClickListener({
            val input_phonenumber = editText_check_phonenumber_fi.text.trim().toString()
            val input_name = editText_check_name_fi.text.trim().toString()

            var existUserDTO : UserDTO? = null
            db.collection("user_information").whereEqualTo("phoneNumber", input_phonenumber).get().addOnCompleteListener{
                if(it.isSuccessful){
                    for(dc in it.result!!.documents){
                        existUserDTO = dc.toObject(UserDTO::class.java)
                        break
                    }
                    if(existUserDTO == null){
                        // 존재하지 않음 ㅠㅠ
                        var builder = AlertDialog.Builder(this)
                        builder.setTitle("일치하는 휴대폰 정보가 없습니다.")
                        builder.setIcon(R.mipmap.ic_launcher)
                        builder.setPositiveButton("확인", null)
                        builder.show()
                        editText_check_phonenumber_fi.setText("")
                    } else{
                        // 이미 존재한다면 ? ID 정보를 알려줘야함!
                        user_name = existUserDTO!!.name.toString()
                        user_email = existUserDTO!!.email.toString()

                        if(user_name == input_name){
                            // Dialog return ID
                            var builder = AlertDialog.Builder(this)
                            builder.setTitle("아이디 = " + user_email)
                            builder.setIcon(R.mipmap.ic_launcher)

                            var listener = DialogInterface.OnClickListener { p0, _ ->
                                editText_check_id_fi.setText(user_email)

                                editText_check_name_fi.setText("")
                                editText_check_phonenumber_fi.setText("")
                            }
                            builder.setPositiveButton("비밀번호 재설정", listener)
                            builder.setNegativeButton("취소", null)
                            builder.show()
                        } else {
                            // 이름 정보가 잘못되었음
                            var builder = AlertDialog.Builder(this)
                            builder.setTitle("휴대폰 정보와 일치하지 않는 이름입니다.")
                            builder.setIcon(R.mipmap.ic_launcher)
                            builder.setPositiveButton("확인", null)
                            builder.show()
                            editText_check_name_fi.setText("")
                        }
                    }
                }else{

                }
            }
        })

        button_find_password.setOnClickListener({
            val input_phonenumber2 = editText_check_phonenumber2_fi.text.trim().toString()
            val input_id = editText_check_id_fi.text.trim().toString()

            var existUserDTO : UserDTO? = null
            db.collection("user_information").whereEqualTo("phoneNumber", input_phonenumber2).get().addOnCompleteListener{
                if(it.isSuccessful){
                    for(dc in it.result!!.documents){
                        existUserDTO = dc.toObject(UserDTO::class.java)
                        break
                    }
                    if(existUserDTO == null){
                        // 존재하지 않음 ㅠㅠ
                        var builder = AlertDialog.Builder(this)
                        builder.setTitle("일치하는 휴대폰 정보가 없습니다.")
                        builder.setIcon(R.mipmap.ic_launcher)
                        builder.setPositiveButton("확인", null)
                        builder.show()
                        editText_check_phonenumber2_fi.setText("")
                    } else{
                        user_name = existUserDTO!!.name.toString()
                        user_email = existUserDTO!!.email.toString()
                        if(user_email == input_id){
                            // 비밀번호 재전송 메일 보내주기
                            var emailAddress : String = user_email

                            firebaseAuth.sendPasswordResetEmail(emailAddress)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(this, "비밀번호 재설정!!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        } else {
                            // 입력 정보가 맞지 않음
                            var builder = AlertDialog.Builder(this)
                            builder.setTitle("아이디와 휴대폰 정보가 일치하지 않습니다.")
                            builder.setIcon(R.mipmap.ic_launcher)
                            builder.setPositiveButton("확인", null)
                            builder.show()
                            editText_check_id_fi.setText("")
                            editText_check_phonenumber2_fi.setText("")
                        }
                    }
                } else{

                }
            }
        })
    }
}
package kr.ac.cau.easyconnect

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// UI만 손보면 될 듯!

class Page_find_information : AppCompatActivity() {
    // 로그인 정보 찾는 페이지 구현
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page_find_information)

        // 권한하고 파이어스토어 데이터베이스 객체 저장
        val firebaseAuth : FirebaseAuth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        // xml 버튼, 텍스트 아이디 연결
        val button_goback : ImageButton = findViewById(R.id.bt_goback)
        val button_find_id : Button = findViewById(R.id.bt_find_id)
        val button_find_password : Button = findViewById(R.id.bt_find_password)
        val editText_check_phonenumber_fi : EditText = findViewById(R.id.edit_check_phonenumber_fi)
        val editText_check_phonenumber2_fi : EditText = findViewById(R.id.edit_check_phonenumber2_fi)
        val editText_check_id_fi : EditText = findViewById(R.id.edit_check_id_fi)
        val editText_check_name_fi : EditText = findViewById(R.id.edit_check_name_fi)

        var user_name : String
        var user_email : String

        button_goback.setOnClickListener({
            val intentLogin = Intent(this, Page_login::class.java)
            startActivity(intentLogin)
            finish()
        })

        button_find_id.setOnClickListener({
            // 아이디 찾기 버튼이 눌렸을 때의 동작 (휴대폰 번호와 이름으로 찾아온다!)
            val input_phonenumber = editText_check_phonenumber_fi.text.trim().toString()
            val input_name = editText_check_name_fi.text.trim().toString()

            var existUserDTO : UserDTO? = null
            db.collection("user_information").whereEqualTo("phoneNumber", input_phonenumber).get().addOnCompleteListener{
                if(it.isSuccessful){
                    // 현재 입력된 휴대폰 번호와 일치하는 UserDTO 객체를 찾는 Query문 수행
                    for(dc in it.result!!.documents){
                        existUserDTO = dc.toObject(UserDTO::class.java)
                        break
                    }
                    if(existUserDTO == null){
                        // 해당 휴대폰 번호로 가입한 정보가 존재하지 않을 때
                        var builder = AlertDialog.Builder(this)
                        builder.setTitle("일치하는 휴대폰 정보가 없습니다.")
                        builder.setPositiveButton("확인", null)
                        builder.show()
                        editText_check_phonenumber_fi.setText("")
                    } else{
                        // 이미 가입한 정보가 존재한다면? ID 정보를 알려줘야함! 하지만 이름도 비교해야하지!
                        user_name = existUserDTO!!.name.toString()
                        user_email = existUserDTO!!.email.toString()

                        if(user_name == input_name){
                            // 만약 이름까지 같다면 아이디(이메일) 정보 띄워서 알려줌
                            // Dialog return ID
                            var builder = AlertDialog.Builder(this)
                            builder.setTitle("아이디 : " + user_email)

                            var listener = DialogInterface.OnClickListener { p0, _ ->
                                editText_check_id_fi.setText(user_email)

                                editText_check_name_fi.setText("")
                                editText_check_phonenumber_fi.setText("")
                            }
                            // 비밀번호 재설정 버튼을 누르면 비밀번호 재설정 파트의 아이디(이메일) 입력란에 자동 입력!
                            builder.setPositiveButton("비밀번호 재설정", listener)
                            builder.setNegativeButton("취소", null)
                            builder.show()
                        } else {
                            // 이름 정보가 잘못 되었을 때!
                            var builder = AlertDialog.Builder(this)
                            builder.setTitle("휴대폰 정보와 일치하지 않는 이름입니다.")
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
            // 비밀번호 재설정 버튼이 눌렸을 때의 동작 (휴대폰 번호와 아이디(이메일)로 재설정!)
            val input_phonenumber2 = editText_check_phonenumber2_fi.text.trim().toString()
            val input_id = editText_check_id_fi.text.trim().toString()

            var existUserDTO : UserDTO? = null
            db.collection("user_information").whereEqualTo("phoneNumber", input_phonenumber2).get().addOnCompleteListener{
                if(it.isSuccessful){
                    // 입력된 휴대폰 번호랑 일치하는 UserDTO 객체가 있는지 데이터베이스에 Query문 수행
                    for(dc in it.result!!.documents){
                        existUserDTO = dc.toObject(UserDTO::class.java)
                        break
                    }
                    if(existUserDTO == null){
                        // 해당 휴대폰 번호로 가입한 정보가 존재하지 않을 때
                        var builder = AlertDialog.Builder(this)
                        builder.setTitle("일치하는 휴대폰 정보가 없습니다.")
                        builder.setPositiveButton("확인", null)
                        builder.show()
                        editText_check_phonenumber2_fi.setText("")
                    } else{
                        // 가입한 정보가 존재한다면 아이디(이메일)가 일치하는지 검사 후 재전송 메일 보내줘야 함!
                        user_name = existUserDTO!!.name.toString()
                        user_email = existUserDTO!!.email.toString()
                        if(user_email == input_id){
                            // 비밀번호 재전송 메일 보내주기
                            var emailAddress : String = user_email

                            firebaseAuth.sendPasswordResetEmail(emailAddress)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(this, "비밀번호 재설정 메일 전송", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        } else {
                            // 아이디(이메일) 정보가 휴대폰 정보와 일치하지 않을 때
                            var builder = AlertDialog.Builder(this)
                            builder.setTitle("아이디와 휴대폰 정보가 일치하지 않습니다.")
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

    override fun onBackPressed(){
        // 클릭 시 이전 페이지인 로그인으로!
        val intentLogin = Intent(this, Page_login::class.java)
        startActivity(intentLogin)
        finish()
    }
}
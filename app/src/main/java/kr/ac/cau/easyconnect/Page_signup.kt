package kr.ac.cau.easyconnect

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User

class Page_signup : AppCompatActivity() {
    // 현재 회원가입 한 페이지로 구현함
    // UI의 편의성을 위해 signup2 사용여지 남아있음 !!!!!!!!!! 2021-04-15 04:39
    var firebaseAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page_signup)

        // Firebase - Auth, Firestore의 인스턴스 받아오기
        firebaseAuth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        // xml id 연결
        var button_fin_signup : Button = findViewById(R.id.bt_fin_signup)
        var editText_phoneNumber : EditText = findViewById(R.id.edit_phoneNumber)
        var editText_name : EditText = findViewById(R.id.edit_name)
        var editText_check_id : EditText = findViewById(R.id.edit_check_id)
        var editText_check_password : EditText = findViewById(R.id.edit_check_password)
        var editText_check_password2 : EditText = findViewById(R.id.edit_check_password2)

        // 회원가입 완료 버튼 눌렀을 때 동작 구현
        button_fin_signup.setOnClickListener({
            // input_xxxx로 입력받은 editText의 text 값들 저장
            var input_password = editText_check_password.text.trim().toString()
            var input_password2 = editText_check_password2.text.trim().toString()
            var input_id = editText_check_id.text.trim().toString()
            var input_phoneNumber = editText_phoneNumber.text.trim().toString()
            var input_name = editText_name.text.trim().toString()

            if (input_password.isNullOrEmpty() || input_password2.isNullOrEmpty() || input_id.isNullOrEmpty() || input_phoneNumber.isNullOrEmpty()) {
                // 공백인 칸이 있다면
                var builder = AlertDialog.Builder(this)
                builder.setTitle("입력 정보가 누락되었습니다.")
                builder.setIcon(R.mipmap.ic_launcher)
                builder.setPositiveButton("확인", null)
                builder.show()
            } else if (input_phoneNumber.length < 10) {
                // 휴대폰 번호 잘못 입력시 (10자리 미만) 오류 후 초기화
                var builder4 = AlertDialog.Builder(this)
                builder4.setTitle("휴대폰 번호를 다시 입력하세요.")
                builder4.setIcon(R.mipmap.ic_launcher)
                builder4.setPositiveButton("확인", null)
                builder4.show()
                editText_phoneNumber.setText("")
            } else {
                // 모든 입력이 잘 되었을 때
                if (input_password != input_password2) {
                    // 입력된 두 비밀번호가 다른 경우 다시 입력받도록 초기화
                    var builder2 = AlertDialog.Builder(this)
                    builder2.setTitle("입력된 두 비밀번호가 다릅니다.")
                    builder2.setIcon(R.mipmap.ic_launcher)
                    builder2.setPositiveButton("확인", null)
                    builder2.show()
                    editText_check_password.setText("")
                    editText_check_password2.setText("")
                } else {
                    var userDTO : UserDTO? = null
                    db.collection("user_information").whereEqualTo("phoneNumber", input_phoneNumber).get().addOnCompleteListener{
                        if(it.isSuccessful){
                            for(dc in it.result!!.documents){
                                userDTO = dc.toObject(UserDTO::class.java)
                                break
                            }
                            if(userDTO == null){
                                // firebase - Auth의 createUserWithEmailAndPassword 메소드 이용하여 계정 생성
                                firebaseAuth!!.createUserWithEmailAndPassword(input_id, input_password)
                                    .addOnCompleteListener(this) {
                                        if (it.isSuccessful) {
                                            // 올바르게 생성
//                                val user = firebaseAuth.currentUser
                                            Toast.makeText(this,"Authentication success.",Toast.LENGTH_SHORT).show()

                                            // userDTO 데이터 클래스 객체에 email, password, name, phoneNumber 저장
                                            val newUserDTO = UserDTO()
                                            newUserDTO.email = input_id
                                            newUserDTO.password = input_password
                                            newUserDTO.name = input_name
                                            newUserDTO.phoneNumber = input_phoneNumber
                                            // firestore에 저장
                                            db.collection("user_information").document(input_phoneNumber)
                                                .set(newUserDTO).addOnCompleteListener(this) {
                                                    if (it.isSuccessful) {
                                                        Toast.makeText(this, "success", Toast.LENGTH_SHORT)
                                                            .show()
                                                    } else {
                                                        Toast.makeText(this, "failed", Toast.LENGTH_SHORT)
                                                            .show()
                                                    }
                                                }

                                            // 아이디 정보 가져가기
                                            val id = input_id
                                            val sharedPreference = getSharedPreferences("other", 0)
                                            val editor = sharedPreference.edit()
                                            editor.putString("id", id)
                                            editor.apply()

                                            // 로그인 페이지로 화면 전환
                                            val intentLogin = Intent(this, Page_login::class.java)
                                            startActivity(intentLogin)
                                            finish()
                                        } else {
                                            // 이미 등록된 이메일의 경우
                                            Toast.makeText(
                                                this,
                                                "Authentication failed.",
                                                Toast.LENGTH_SHORT
                                            )
                                                .show()
                                            var builder3 = AlertDialog.Builder(this)
                                            builder3.setTitle("이미 등록된 이메일입니다.")
                                            builder3.setIcon(R.mipmap.ic_launcher)
                                            builder3.setPositiveButton("확인", null)
                                            builder3.show()

                                            // 이메일 다시 입력
                                            editText_check_id.setText("")
                                        }
                                    }
                            } else{
                                var builder5 = AlertDialog.Builder(this)
                                builder5.setTitle("이미 존재하는 휴대폰 번호입니다.")
                                builder5.setIcon(R.mipmap.ic_launcher)
                                builder5.setPositiveButton("확인", null)
                                builder5.show()
                                editText_phoneNumber.setText("")
                            }
                        }
                    }
                }
            }

        })

    }


}
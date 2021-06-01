package kr.ac.cau.easyconnect

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// UI 직관성 높이려면 회원가입할 때 나눠야 할 것 같아서 처음에 나누었음
// 하지만 데이터 받는데 불편해서 일단 하나로 통합
// 현재 사용 X !!!!!!!!!!!! 2021-04-15 04:39

class Page_signup2 : AppCompatActivity() {

    private var firebaseAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page_signup2)

        var button_fin_signup : Button = findViewById(R.id.bt_fin_signup)
        var editText_phoneNumber : EditText = findViewById(R.id.edit_phoneNumber)
        var editText_name : EditText = findViewById(R.id.edit_name)
        var editText_age : EditText = findViewById(R.id.edit_age)
        var radioGroup : RadioGroup = findViewById(R.id.rg_gender)

        firebaseAuth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        val sharedPreference = getSharedPreferences("signup", 0)
        val newId = sharedPreference.getString("id", "")
        val newPw = sharedPreference.getString("password", "")
        var newGender : String? = null

        radioGroup.setOnCheckedChangeListener{ group, checkId ->
            if(checkId == R.id.gender_male){
                newGender = "male"
                Toast.makeText(this, "남자", Toast.LENGTH_SHORT).show()
            }
            if(checkId == R.id.gender_female){
                newGender = "female"
                Toast.makeText(this, "여자", Toast.LENGTH_SHORT).show()
            }
        }

        button_fin_signup.setOnClickListener({
            var input_phoneNumber = editText_phoneNumber.text.trim().toString()
            var input_name = editText_name.text.trim().toString()
            var input_age = editText_age.text.trim().toString()

            if (input_name.isNullOrEmpty() || input_phoneNumber.isNullOrEmpty() || newGender.isNullOrEmpty()) {
                // 공백인 칸이 있다면
                var builder = AlertDialog.Builder(this)
                builder.setTitle("입력 정보가 누락되었습니다.")
                builder.setPositiveButton("확인", null)
                builder.show()
            } else if (input_phoneNumber.length < 10) {
                // 휴대폰 번호 잘못 입력시 (10자리 미만) 오류 후 초기화
                var builder4 = AlertDialog.Builder(this)
                builder4.setTitle("휴대폰 번호를 다시 입력하세요.")
                builder4.setPositiveButton("확인", null)
                builder4.show()
                editText_phoneNumber.setText("")
            } else {
                // 모든 입력이 잘 되었을 때
                var userDTO : UserDTO? = null // 회원 가입 정보를 담아 둘 UserDTO 데이터 클래스 객체 생성
                db.collection("user_information").whereEqualTo("phoneNumber", input_phoneNumber).get().addOnCompleteListener {
                    if (it.isSuccessful) { // query 잘 수행 되면 여기로 들어옴
                        for (dc in it.result!!.documents) {
                            userDTO = dc.toObject(UserDTO::class.java)
                            break
                        }
                        if (userDTO == null) {
                            // 휴대폰 번호로 조회했을 때 이전에 가입되지 않은 번호라면 새로운 UserDTO 객체를 데이터베이스에 넣어줘야함 // firebase - Auth의 createUserWithEmailAndPassword 메소드 이용하여 계정 생성
                            firebaseAuth!!.createUserWithEmailAndPassword(newId, newPw)
                                .addOnCompleteListener(this) {
                                    if (it.isSuccessful) {
                                        // 올바르게 생성 되었다!

                                        // 하지만 이메일이 실제 사용되는 건가에 대한 인증이 필요함
                                        firebaseAuth!!.currentUser.sendEmailVerification()
                                            .addOnCompleteListener { verifiTask ->
                                                if (verifiTask.isSuccessful) {
                                                    Toast.makeText(
                                                        this,
                                                            "이메일 인증후 이용 가능합니다.",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    // userDTO 데이터 클래스 객체에 email, password, name, phoneNumber 은 입력값, uid와 photo 는 기본값 저장
                                                    // age는 입력을 하지 않으면 "" 빈 값으로 들어감 <- 추천 목적
                                                    var newUserDTO = UserDTO()
                                                    newUserDTO.email = newId
                                                    newUserDTO.password = newPw
                                                    newUserDTO.name = input_name
                                                    newUserDTO.phoneNumber = input_phoneNumber
                                                    newUserDTO.photo = "base.jpg"
                                                    newUserDTO.uid = firebaseAuth!!.uid
                                                    newUserDTO.search = false
                                                    newUserDTO.followed = ""
                                                    newUserDTO.following = ""
                                                    if(input_age.isNullOrEmpty()){
                                                        newUserDTO.age = "0"
                                                    }else{
                                                        newUserDTO.age = input_age
                                                    }
                                                    newUserDTO.gender = newGender

                                                    // firestore에 newUserDTO 객체 저장
                                                    db.collection("user_information")
                                                        .document(firebaseAuth!!.uid.toString())
                                                        .set(newUserDTO)

                                                    // 아이디 정보 가져가기 (로그인 페이지와 공유함)
                                                    val id = newId
                                                    val sharedPreference =
                                                        getSharedPreferences("other", 0)
                                                    val editor = sharedPreference.edit()
                                                    editor.putString("id", id)
                                                    editor.apply()

                                                    val sharedPreference2 = getSharedPreferences("logout", 0)
                                                    val editor2 = sharedPreference2.edit()
                                                    editor2.putBoolean("islogout", true)
                                                    editor2.apply()
                                                        // 로그인 페이지로 화면 전환
                                                    val intentLogin =
                                                        Intent(this, Page_login::class.java)
                                                    startActivity(intentLogin)
                                                    finish()
                                                }
                                            }
                                        } else {
                                            // 이미 등록된 이메일의 경우!!
                                            // 앞 페이지에서 이미 검증함함
                                       }
                                    }
                            } else {
                                var builder5 = AlertDialog.Builder(this)
                                builder5.setTitle("이미 존재하는 휴대폰 번호입니다.")
                                builder5.setPositiveButton("확인", null)
                                builder5.show()
                                editText_phoneNumber.setText("")
                            }
                        }
                    }
            }

        })

    }

}
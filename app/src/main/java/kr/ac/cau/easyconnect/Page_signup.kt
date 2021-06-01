package kr.ac.cau.easyconnect

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User

// UI만 손보면 될 듯!

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
        var button_goback : ImageButton = findViewById(R.id.bt_goback)
        var button_next : Button = findViewById(R.id.bt_next)
        var editText_check_id : EditText = findViewById(R.id.edit_check_id)
        var editText_check_password : EditText = findViewById(R.id.edit_check_password)
        var editText_check_password2 : EditText = findViewById(R.id.edit_check_password2)

        button_goback.setOnClickListener({
            // 뒤로 버튼 클릭 시 되돌아가는 여부 체크하고 확인 누르면 로그인 페이지로 이동
            var builder_dialog = AlertDialog.Builder(this);
            builder_dialog.setTitle("내용이 저장되지 않습니다. 돌아가시겠습니까?"); // 다이얼로그 제목
            var listener = DialogInterface.OnClickListener { dialog, which ->
                val sharedPreference = getSharedPreferences("logout", 0)
                val editor = sharedPreference.edit()
                editor.putBoolean("islogout", true)
                editor.apply()

                val intentLogin = Intent(this, Page_login::class.java)
                startActivity(intentLogin)
                finish()
            }
            builder_dialog.setPositiveButton("확인", listener)
            builder_dialog.setNegativeButton("취소", null)
            builder_dialog.show(); // 다이얼로그 보이기
        })

        button_next.setOnClickListener({
            var input_password = editText_check_password.text.trim().toString()
            var input_password2 = editText_check_password2.text.trim().toString()
            var input_id = editText_check_id.text.trim().toString()

            var userDTO = UserDTO()
            db.collection("user_information").whereEqualTo("email", input_id).get().addOnCompleteListener {
                if(it.isSuccessful){
                    for (dc in it.result!!.documents) {
                        userDTO = dc.toObject(UserDTO::class.java)!!
                        break
                    }
                    if (input_password.isNullOrEmpty() || input_password2.isNullOrEmpty() || input_id.isNullOrEmpty()) {
                        // 공백인 칸이 있다면
                        var builder = AlertDialog.Builder(this)
                        builder.setTitle("입력 정보가 누락되었습니다.")
                        builder.setPositiveButton("확인", null)
                        builder.show()
                    } else {
                        // 모든 입력이 잘 되었을 때
                        if (input_password != input_password2) {
                            // 입력된 두 비밀번호가 다른 경우 다시 입력받도록 초기화
                            var builder2 = AlertDialog.Builder(this)
                            builder2.setTitle("입력된 두 비밀번호가 다릅니다.")
                            builder2.setPositiveButton("확인", null)
                            builder2.show()
                            editText_check_password.setText("")
                            editText_check_password2.setText("")
                        }else if (input_id == userDTO.email){
                            var builder2 = AlertDialog.Builder(this)
                            builder2.setTitle("이미 존재하는 이메일입니다.")
                            builder2.setPositiveButton("확인", null)
                            builder2.show()
                            editText_check_id.setText("")
                        }else {
                            val id = input_id
                            val password = input_password
                            val sharedPreference = getSharedPreferences("signup", 0)
                            val editor = sharedPreference.edit()
                            editor.putString("id", id)
                            editor.putString("password", password)
                            editor.apply()

                            val intentSignup2 = Intent(this, Page_signup2::class.java)
                            startActivity(intentSignup2)
                        }
                    }
                }
            }
        })

    }

    override fun onBackPressed() {
        // 뒤로 버튼 클릭 시 되돌아가는 여부 체크하고 확인 누르면 로그인 페이지로 이동
        var builder_dialog = AlertDialog.Builder(this);
        builder_dialog.setTitle("내용이 저장되지 않습니다. 돌아가시겠습니까?"); // 다이얼로그 제목
        var listener = DialogInterface.OnClickListener { dialog, which ->
            val sharedPreference = getSharedPreferences("logout", 0)
            val editor = sharedPreference.edit()
            editor.putBoolean("islogout", true)
            editor.apply()

            val intentLogin = Intent(this, Page_login::class.java)
            startActivity(intentLogin)
            finish()
        }
        builder_dialog.setPositiveButton("확인", listener)
        builder_dialog.setNegativeButton("취소", null)
        builder_dialog.show(); // 다이얼로그 보이기
    }
}
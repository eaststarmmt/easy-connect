package kr.ac.cau.easyconnect

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.google.firebase.auth.FirebaseAuth

class Page_login : AppCompatActivity() {// DB로부터 받아와야 할 듯 함

    var firebaseAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page_login)

        // firebase - Auth의 인스턴스 받아오기
        firebaseAuth = FirebaseAuth.getInstance();

        // xml id 연결
        val button_login : Button = findViewById(R.id.bt_login)
        val button_signup : Button = findViewById(R.id.bt_signup)
        val button_find_info : Button = findViewById(R.id.bt_find_info)
        val editText_id : EditText = findViewById(R.id.edit_id)
        val editText_password : EditText = findViewById(R.id.edit_password)

        val sharedPreference = getSharedPreferences("other", 0)
        editText_id.setText(sharedPreference.getString("id", ""))

        button_login.setOnClickListener({
            // 로그인 버튼이 눌렸을 때 동작
            // id와 password에 입력된 text 내용 저장
            var id = editText_id.text.toString()
            var password = editText_password.text.toString()

            if(id.isNullOrEmpty() || password.isNullOrEmpty()){
                // 공백이 있을 때 오류 출력
                var builder = AlertDialog.Builder(this)
                builder.setTitle("입력 정보가 누락되었습니다.")
                builder.setIcon(R.mipmap.ic_launcher)
                builder.setPositiveButton("확인", null)
                builder.show()
            }
            else {
                // firebase - Auth 의 signInWithEmailAndPassword 메소드 이용하여 로그인 기능 구현
                firebaseAuth!!.signInWithEmailAndPassword(
                    editText_id.text.toString(),
                    editText_password.text.toString()
                ).addOnCompleteListener(this) {
                    if (it.isSuccessful) {
                        // 로그인 정보가 맞을 때 동작
                        //val user = firebaseAuth?.currentUser
                        Toast.makeText(this, "SignInWithEmail success.", Toast.LENGTH_SHORT).show()

                        val saved_id = id
                        val editor = sharedPreference.edit()
                        editor.clear()
                        editor.putString("id", saved_id)
                        editor.apply()

                        // 메인 페이지로 이동 (나의 글 목록 포토카드로 보여주는 페이지)
                        val intentMain = Intent(this, MainActivity::class.java)
                        startActivity(intentMain)
                        finish()
                        //updateUI(user)
                    } else {
                        // 입력된 아이디와 비밀번호가 로그인 정보와 맞지 않는 경우 오류 출력 후 초기화
                        var builder = AlertDialog.Builder(this)
                        builder.setTitle("틀렸습니다. 다시 입력하세요.")
                        builder.setIcon(R.mipmap.ic_launcher)
                        builder.setPositiveButton("확인", null)
                        builder.show()
                        editText_id.setText("")
                        editText_password.setText("")
                    }
                }
            }
        })

        button_signup.setOnClickListener({
            // 버튼 눌렸을 때 회원가입 페이지로 이동
            val intentSignUp = Intent(this, Page_signup::class.java)
            startActivity(intentSignUp)
        })

        button_find_info.setOnClickListener({
            // 버튼 눌렀을 때 아이디 비밀번호 찾는 페이지로 이동
            val intentFindInformations = Intent(this, Page_find_information::class.java)
            startActivity(intentFindInformations)

            // val intentFindInfo = Intent(this, Page_findInfo::class.java)
            // startActivity(intentFindInfo)
        })


    }

    override fun onBackPressed(){
        // 회원탈퇴 후 뒤로가는 기능 막기 위함
        // 클릭 시 종료 여부 체크하고 종료 버튼 누르면 앱 종료
        var builder_dialog = AlertDialog.Builder(this);
        builder_dialog.setTitle("종료할까요?"); // 다이얼로그 제목
        builder_dialog.setIcon(R.mipmap.ic_launcher)
        var listener = DialogInterface.OnClickListener { dialog, which
            -> ActivityCompat.finishAffinity(this)
            System.exit(0)
        }
        builder_dialog.setPositiveButton("종료", listener)
        builder_dialog.setNegativeButton("취소", null)
        builder_dialog.show(); // 다이얼로그 보이기
    }
}
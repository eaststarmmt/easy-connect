package kr.ac.cau.easyconnect

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth

// 환경설정 부분 구현 빼고는 다 한듯?

class Page_menu : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page_menu)

        val button_goback : ImageButton = findViewById(R.id.bt_goback)
        val button_find_friend : Button = findViewById(R.id.bt_find_friend)
        val button_logout : Button = findViewById(R.id.bt_logout)
        val button_mypage : Button = findViewById(R.id.bt_mypage)

        button_goback.setOnClickListener({
            val intentMain = Intent(this, MainActivity::class.java)
            startActivity(intentMain)
            finish()
        })

        button_mypage.setOnClickListener({
            // 버튼 눌렸을 때 마이페이지 이동
            val intentMypage = Intent(this, Page_mypage::class.java)
            startActivity(intentMypage)
            finish()
        })

        button_find_friend.setOnClickListener({
            // 버튼 눌렸을 때 친구 정보 검색 가능한 페이지로 이동
            val intentFindFriends = Intent(this, Page_find_friends::class.java)
            startActivity(intentFindFriends)
            finish()
        })

        button_logout.setOnClickListener({
            // 버튼 눌렸을 때 로그아웃 여부 확인하기
            var builder_dialog = AlertDialog.Builder(this);
            builder_dialog.setTitle("로그아웃 하시겠습니까?"); // 다이얼로그 제목
            var listener = DialogInterface.OnClickListener { dialog, which ->

                val sharedPreference = getSharedPreferences("logout", 0)
                val editor = sharedPreference.edit()
                editor.putBoolean("islogout", true)
                editor.apply()

                val intentLogin = Intent(this, Page_login::class.java)

                FirebaseAuth.getInstance().signOut()
                startActivity(intentLogin)
                finish()
            }
            builder_dialog.setPositiveButton("확인", listener)
            builder_dialog.setNegativeButton("취소", null)
            builder_dialog.show(); // 다이얼로그 보이기
        })
    }

    override fun onBackPressed(){
        // 클릭 시 이전 페이지인 메인으로!
        val intentMain = Intent(this, MainActivity::class.java)
        startActivity(intentMain)
        finish()
    }
}
package kr.ac.cau.easyconnect

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth

// 환경설정 부분 구현 빼고는 다 한듯?

class Page_menu : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page_menu)

        val button_goback : Button = findViewById(R.id.bt_goback)
        val button_find_friend : Button = findViewById(R.id.bt_find_friend)
//        val button_setting : Button = findViewById(R.id.bt_setting)
        val button_logout : Button = findViewById(R.id.bt_logout)
        val button_mypage : Button = findViewById(R.id.bt_mypage)

        button_goback.setOnClickListener({
            val intentMain = Intent(this, MainActivity::class.java)
            startActivity(intentMain)
        })

        button_mypage.setOnClickListener({
            // 버튼 눌렸을 때 마이페이지 이동
            val intentMypage = Intent(this, Page_mypage::class.java)
            startActivity(intentMypage)
        })

        button_find_friend.setOnClickListener({
            // 버튼 눌렸을 때 친구 정보 검색 가능한 페이지로 이동
            val intentFindFriends = Intent(this, Page_find_friends::class.java)
            startActivity(intentFindFriends)
        })

//        button_setting.setOnClickListener({
//            // 버튼 눌렸을 때 셋팅(글자 크기, 알림 끄기 등) 페이지로 이동 !!!!!!!!!! 개인정보 보호를 위해 검색 안되게 막는 것 기능으로 넣으면 좋을 듯
//            val intentSets = Intent(this, Page_sets::class.java)
//            startActivity(intentSets)
//        })

        button_logout.setOnClickListener({
            // 버튼 눌렸을 때 비밀번호 확인하고 로그아웃 (완료)
            FirebaseAuth.getInstance().signOut()

            val intentLogin = Intent(this, Page_login::class.java)
            startActivity(intentLogin)
            finish()
        })

    }
}
package kr.ac.cau.easyconnect

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast

class Page_menu : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page_menu)

        val button_goback : Button = findViewById(R.id.bt_goback)
        val button_find_friend : Button = findViewById(R.id.bt_find_friend)
        val button_setting : Button = findViewById(R.id.bt_setting)
        val button_withdrawal : Button = findViewById(R.id.bt_withdrawal)

        button_goback.setOnClickListener({
            val intentMain = Intent(this, MainActivity::class.java)
            startActivity(intentMain)
        })

        button_find_friend.setOnClickListener({
            // 버튼 눌렸을 때 친구 정보 검색 가능하게 구현
            val intentFindFriends = Intent(this, Page_find_friends::class.java)
            startActivity(intentFindFriends)
        })

        button_setting.setOnClickListener({
            // 버튼 눌렸을 때 셋팅(글자 크기, 알림 끄기 등) 구현
            val intentSets = Intent(this, Page_sets::class.java)
            startActivity(intentSets)
        })

        button_withdrawal.setOnClickListener({
            // 버튼 눌렸을 때 비밀번호 확인하고 회원탈퇴 기능 구현
            // 일단은 바로 로그인 페이지로 이동하도록
            val text_withdrawal = "회원 탈퇴 완료!"
            val alarm_withdrawal = Toast.makeText(applicationContext, text_withdrawal, Toast.LENGTH_SHORT)
            alarm_withdrawal.show()

            val intentLogin = Intent(this, Page_login::class.java)
            startActivity(intentLogin)
        })

    }
}
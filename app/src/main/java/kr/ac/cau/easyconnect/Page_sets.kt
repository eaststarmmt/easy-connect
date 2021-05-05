package kr.ac.cau.easyconnect

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton

// 구현 할지 말지 미정

class Page_sets : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page_sets)

        val button_goback : ImageButton = findViewById(R.id.bt_goback)

        button_goback.setOnClickListener({
            val intentMenu = Intent(this, Page_menu::class.java)
            startActivity(intentMenu)
            finish()
        })

        // 셋팅 부분 구현해야함!!           알림 키고 끄기 / 폰트크기 / 본인 정보 검색 가능하게 할 건지?
    }

    override fun onBackPressed(){
        // 클릭 시 이전 페이지인 메인으로!
        val intentMenu = Intent(this, Page_menu::class.java)
        startActivity(intentMenu)
        finish()
    }
}
package kr.ac.cau.easyconnect

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.widget.Button
import android.widget.TabHost
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat

// 미완 아직 개발 단계 아님

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button_menu : Button = findViewById(R.id.bt_menu)

        val host : TabHost = findViewById(R.id.host)
        host.setup()

        val tabSpec1 : TabHost.TabSpec = host.newTabSpec("Tab Spec 1")
        tabSpec1.setContent(R.id.content1)
        tabSpec1.setIndicator("타임라인")
        host.addTab(tabSpec1)

        val tabSpec2 : TabHost.TabSpec = host.newTabSpec("Tab Spec 2")
        tabSpec2.setContent(R.id.content2)
        tabSpec2.setIndicator("친구들")
        host.addTab(tabSpec2)

        val tabSpec3 : TabHost.TabSpec = host.newTabSpec("Tab Spec 3")
        tabSpec3.setContent(R.id.content3)
        tabSpec3.setIndicator("추천 키워드")
        host.addTab(tabSpec3)
        // 메인 뷰 (자신의 글을 포토카드 형식으로 출력) / 마지막에 게시글 추가 버튼 !!!!!!!!!!!! 구현해야함 아직 아님

        var builder_dialog = AlertDialog.Builder(this);
        builder_dialog.setTitle("잘 안보인다면 글자 크기를 변경하세요!"); // 다이얼로그 제목
        builder_dialog.setPositiveButton("확인", null)
        builder_dialog.show(); // 다이얼로그 보이기


        button_menu.setOnClickListener({
            val intentMenu = Intent(this, Page_menu::class.java)
            startActivity(intentMenu)
            finish()
        })
        findViewById<Button>(R.id.bt_post).setOnClickListener{
            val intent = Intent(this, WriteActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onBackPressed(){
        // 회원탈퇴 후 뒤로가는 기능 막기 위함
        // 클릭 시 종료 여부 체크하고 종료 버튼 누르면 앱 종료
        var builder_dialog = AlertDialog.Builder(this);
        builder_dialog.setTitle("종료할까요?"); // 다이얼로그 제목
        builder_dialog.setIcon(R.mipmap.easy_connect)
        var listener = DialogInterface.OnClickListener { dialog, which
            -> ActivityCompat.finishAffinity(this)
            System.exit(0)
        }
        builder_dialog.setPositiveButton("종료", listener)
        builder_dialog.setNegativeButton("취소", null)
        builder_dialog.show(); // 다이얼로그 보이기
    }

}
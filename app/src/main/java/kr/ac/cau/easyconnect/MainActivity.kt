package kr.ac.cau.easyconnect

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat

// 미완 아직 개발 단계 아님

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button_menu : Button = findViewById(R.id.bt_menu)

        // 메인 뷰 (자신의 글을 포토카드 형식으로 출력) / 마지막에 게시글 추가 버튼 !!!!!!!!!!!! 구현해야함 아직 아님


        button_menu.setOnClickListener({
            val intentMenu = Intent(this, Page_menu::class.java)
            startActivity(intentMenu)
            finish()
        })

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
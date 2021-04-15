package kr.ac.cau.easyconnect

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.w3c.dom.Text

class Page_mypage : AppCompatActivity() {
    // 마이페이지 구현
    var firebaseAuth : FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page_mypage)

        firebaseAuth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance() // 현재 안쓰지만 쓸 여지 남아있어서 놔둠 (비밀번호 입력받아서 비교하려면 필요할 듯?) 2021-04-15 04:39

        val button_change_img : Button = findViewById(R.id.bt_change_img)
        val button_change_password : Button = findViewById(R.id.bt_change_password)
        val button_withdrawal : Button = findViewById(R.id.bt_withdrawal)

        // 현재 정보를 기본적으로 출력하는 걸 구현해야 함!!!!!!!!!!!!!!!! 2021-04-15 04:39

        button_change_img.setOnClickListener({
            // 이미지 변경 기능 2021-04-15 04:39
        })

        button_change_password.setOnClickListener({
            // 비밀번호 변경 기능 2021-04-15 04:39
            val dialog = UpdatePasswordDialog(this)
            dialog.myDiag()
            var input_password : String? = null

            dialog.setOnClickedListener(object : UpdatePasswordDialog.ButtonClickListener{
                override fun onClicked(password: String) {
                    input_password = password
                }
            })
            firebaseAuth.currentUser.updatePassword(input_password).addOnCompleteListener(this){
                if(it.isSuccessful){
                    // 비밀번호 변경 성공
                    Toast.makeText(this, "비밀번호 변경 성공!!", Toast.LENGTH_SHORT).show()
                    // 파이어스토어 변경
                }
            }
        })

        button_withdrawal.setOnClickListener({
            // 회원 탈퇴 기능
            var builder = AlertDialog.Builder(this)
            builder.setTitle("정말로 탈퇴하시겠습니까?")
            builder.setIcon(R.mipmap.ic_launcher)

            var listener = DialogInterface.OnClickListener { p0, _ ->
                firebaseAuth!!.currentUser.delete()
                val intentLogin = Intent(this, Page_login::class.java)
                startActivity(intentLogin)
                finish()
            }

            builder.setPositiveButton("확인", listener)
            builder.setNegativeButton("취소", null)
            builder.show()

            /////////////////////// 데이터베이스 firestore에서도 해당 회원 정보 삭제해야함!!!!!!!!!!!!!!!!!! 2021-04-15 04:39

        })
    }
}
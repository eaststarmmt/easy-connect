package kr.ac.cau.easyconnect

import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.LocalDateTime

class WriteActivity : AppCompatActivity() {

    var firebaseAuth: FirebaseAuth? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write)

        // Firebase - Auth, Firestore의 인스턴스 받아오기
        firebaseAuth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        // xml id 연결
        val title: EditText = findViewById(R.id.title)
        val content: EditText = findViewById(R.id.content)
        findViewById<Button>(R.id.back).setOnClickListener {
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle("작성된 내용이 저장되지 않습니다. \n종료하시겠습니까? ")
            // 확인시 종료 처리 할 리스너
            var listener = DialogInterface.OnClickListener { dialog, i ->
                finish()
            }
            dialog.setPositiveButton("확인", listener)
            dialog.setNegativeButton("취소", null)
            dialog.show()
        }
        // 취소버튼 눌렀을 때 구현
        findViewById<Button>(R.id.cancel).setOnClickListener {
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle("작성된 내용이 저장되지 않습니다. \n종료하시겠습니까? ")
            // 확인시 종료 처리 할 리스너
            var listener = DialogInterface.OnClickListener { dialog, i ->
                finish()
            }
            dialog.setPositiveButton("확인", listener)
            dialog.setNegativeButton("취소", null)
            dialog.show()
        }

        // 게시버튼 눌렀을 때 구현
       findViewById<Button>(R.id.post).setOnClickListener {
            var inputTitle = title.text.trim().toString()
            var inputContent = content.text.trim().toString()
            var name = firebaseAuth!!.currentUser.email.toString()
            var registered : String = LocalDateTime.now().toString()
            var modified : String = LocalDateTime.now().toString()

            val postDTO : PostDTO = PostDTO(inputTitle, inputContent, name, registered, modified)

           if (inputTitle.isNullOrEmpty()) {
               var builder = AlertDialog.Builder(this)
               builder.setTitle("제목을 입력해주세요.")
               builder.setPositiveButton("확인", null)
               builder.show()
           } else if (inputContent.isNullOrEmpty()) {
               var builder = AlertDialog.Builder(this)
               builder.setTitle("내용을 입력해주세요.")
               builder.setPositiveButton("확인", null)
               builder.show()
           } else {
               db.collection("post").document(inputTitle).set(postDTO).addOnCompleteListener(this) {
                   //글이 정상적으로 작성 됐을 때
                   if (it.isSuccessful) {
                       Toast.makeText(this, "success", Toast.LENGTH_SHORT).show()
                       //현재 엑티비티 종료하고 내가 쓴 글 확인하는 액티비티로 이동. 추후에 수정 예정
                       val intent = Intent(this, DetailActivity::class.java)
                       intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                       startActivity(intent)
                       finish()
                   } else {
                       Toast.makeText(this, "failed", Toast.LENGTH_SHORT).show()
                   }
               }
           }
       }

    }
    override fun onBackPressed(){
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("작성된 내용이 저장되지 않을수 있습니다.\n종료하시겠습니까? ")
        // 확인시 종료 처리 할 리스너
        var listener = DialogInterface.OnClickListener { dialog, i ->
            finish()
        }
        dialog.setPositiveButton("확인", listener)
        dialog.setNegativeButton("취소", null)
        dialog.show()
    }
}

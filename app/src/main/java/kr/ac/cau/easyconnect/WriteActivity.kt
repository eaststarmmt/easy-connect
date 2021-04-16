package kr.ac.cau.easyconnect

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
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
        val cancel: Button = findViewById(R.id.cancel)
        val back: Button = findViewById(R.id.back)

        // 게시버튼 눌렀을 때 구현
       findViewById<Button>(R.id.post).setOnClickListener {
            var inputTitle = title.text.trim().toString()
            var inputContent = content.text.trim().toString()
            var name = firebaseAuth!!.currentUser.email.toString()
            var registered : String = LocalDateTime.now().toString()
            var modified : String = LocalDateTime.now().toString()

            val postDTO : PostDTO = PostDTO(inputTitle, inputContent, name, registered, modified)
            db.collection("post").document(inputTitle)
                    .set(postDTO).addOnCompleteListener(this) {
                        if (it.isSuccessful) {
                            Toast.makeText(this, "success", Toast.LENGTH_SHORT)
                                    .show()
                        } else {
                            Toast.makeText(this, "failed", Toast.LENGTH_SHORT)
                                    .show()
                        }
            }
           val intent = Intent(this, DetailActivity::class.java)
           startActivity(intent)
        }
    }
}
package kr.ac.cau.easyconnect

import android.content.DialogInterface
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.time.LocalDateTime

class UpdateActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update)

        var firebaseAuth: FirebaseAuth? = null
        var storage: FirebaseStorage? = null

        // Firebase - Auth, Firestore의 인스턴스 받아오기
        firebaseAuth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        var title: EditText = findViewById(R.id.editTitle)
        val content: EditText = findViewById(R.id.editContent)
        var postDTO: PostDTO? = null

        db.collection("post").whereEqualTo("name", firebaseAuth!!.currentUser.email).get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    // 파이어스토어에서 현재 게시글 정보 조회
                    storage = FirebaseStorage.getInstance()
                    val storageReference = storage!!.reference

                    for (dc in it.result!!.documents.reversed()) {
                        postDTO = dc.toObject(PostDTO::class.java)
                        break
                    }
                    // 게시글 정보 받아오기
                    if (postDTO != null) {
                        title.setText(postDTO!!.title)
                        content.setText(postDTO!!.content)
                    }
                }
            }

        findViewById<Button>(R.id.update).setOnClickListener {
            db.collection("post").document(postDTO!!.title.toString()).delete()
            postDTO!!.title = title.text.trim().toString()
            postDTO!!.content = content.text.trim().toString()
            postDTO!!.modified = LocalDateTime.now().toString()
            db.collection("post").document(postDTO!!.title.toString()).set(postDTO!!)
        }

        findViewById<Button>(R.id.cancel).setOnClickListener {
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

        findViewById<ImageButton>(R.id.back).setOnClickListener {
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
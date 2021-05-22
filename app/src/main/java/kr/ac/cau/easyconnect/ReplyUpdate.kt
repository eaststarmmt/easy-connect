package kr.ac.cau.easyconnect

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ReplyUpdate : AppCompatActivity() {
    var storage: FirebaseStorage? = null
    var firebaseAuth: FirebaseAuth? = null
    var db: FirebaseFirestore? = null
    var path : String? = null
    var id : String? = null
    var replyDTO : ReplyDTO? = null
    var editReply : EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reply_update)
        storage = FirebaseStorage.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        path = intent.getStringExtra("path")  // reply path 인텐트로 넘겨받음
        id = intent.getStringExtra("id")
        editReply = findViewById(R.id.editReply)
        //editReply!!.setText(id)

        db!!.collection(path.toString()).whereEqualTo("modified", id).get().addOnCompleteListener {
            if (it.isSuccessful) {
                storage = FirebaseStorage.getInstance()
                val storageReference = storage!!.reference

                for (dc in it.result!!.documents) {
                    replyDTO = dc.toObject(ReplyDTO::class.java)
                    break
                }
                if (replyDTO != null) {
                    editReply!!.setText(replyDTO!!.content)
                }
            }
        }
        // 수정버튼 눌렀을 때
        findViewById<Button>(R.id.replyUpdate).setOnClickListener {
            // 수정할때는 map 사용 해야됨
            var map = mutableMapOf<String, Any?>()
            map["content"] = editReply!!.text.trim().toString()
            db!!.collection(path.toString()).document(replyDTO!!.registered.toString()).update(map)
                .addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(this, "수정 완료", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
}
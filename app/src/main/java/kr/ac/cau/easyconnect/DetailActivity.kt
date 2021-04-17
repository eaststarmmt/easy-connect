package kr.ac.cau.easyconnect

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class DetailActivity : AppCompatActivity() {
    //테스트용. 삭제할 페이지
    var firebaseAuth: FirebaseAuth? = null
    var storage : FirebaseStorage? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        // Firebase - Auth, Firestore의 인스턴스 받아오기
        firebaseAuth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val title : TextView = findViewById(R.id.title)
        val content : TextView = findViewById(R.id.content)
        var postDTO : PostDTO? = null
        db.collection("post").whereEqualTo("name", firebaseAuth!!.currentUser.email).get().addOnCompleteListener {
            if(it.isSuccessful) {
                // 파이어스토어에서 현재 게시글 정보 조회
                storage = FirebaseStorage.getInstance()
                val storageReference = storage!!.reference

                for(dc in it.result!!.documents.reversed()) {
                    postDTO = dc.toObject(PostDTO::class.java)
                    break
                }
                // 게시글 정보 받아오기
                if (postDTO != null) {
                    title.text = postDTO!!.title
                    content.text = postDTO!!.content
                }
            }
        }
        findViewById<Button>(R.id.update).setOnClickListener {
            val intent = Intent(this, UpdateActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }
    }
}
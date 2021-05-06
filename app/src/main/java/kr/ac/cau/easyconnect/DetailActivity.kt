package kr.ac.cau.easyconnect

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class DetailActivity : AppCompatActivity() {
    //테스트용. 삭제할 페이지
    var firebaseAuth: FirebaseAuth? = null
    var storage : FirebaseStorage? = null
    var imgFileName: String? = null
    private lateinit var imgView : ImageView
    // 형석
//    lateinit var item : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        // Firebase - Auth, Firestore의 인스턴스 받아오기
        firebaseAuth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val title : TextView = findViewById(R.id.title)
        val content : TextView = findViewById(R.id.content)
        var postDTO : PostDTO? = null
        imgView = findViewById(R.id.imgView)

        // 형석
//        item = intent.getStringExtra("data") as String
//        var item_split = item.split(" ")
//        var item_name = item_split[0]
//        var item_modified = item_split[1]

        db.collection("post").whereEqualTo("name", firebaseAuth!!.currentUser.email).get().addOnCompleteListener {
            if(it.isSuccessful) {
                // 파이어스토어에서 현재 게시글 정보 조회
//                var thisData : PostDTO? = null
//                for(dc in it.result!!.documents.reversed()) {
//                    var data = dc.toObject(PostDTO::class.java)
//                    if(data!!.modified == item_modified && data.name == item_name){
//                        thisData = data
//                        break
//                    }
//                }
//
//                if (thisData != null) {
//                    title.text = thisData!!.title
//                    content.text = thisData!!.content
//                }

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
                    imgFileName = postDTO!!.imageOfDetail
                    storageReference.child("post/" + postDTO!!.imageOfDetail.toString()).downloadUrl.addOnSuccessListener {
                        Glide.with(this)
                            .load(it)
                            .into(imgView)
                    }
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

    override fun onBackPressed(){
        // 클릭 시 이전 페이지인 메인으로!
        val intentMain = Intent(this, MainActivity::class.java)
        startActivity(intentMain)
        finish()
    }
}
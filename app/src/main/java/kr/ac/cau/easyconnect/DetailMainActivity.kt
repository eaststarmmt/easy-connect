package kr.ac.cau.easyconnect

import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.fragment.app.FragmentTransaction
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.myhome.siviewpager.SIViewPager
import me.relex.circleindicator.CircleIndicator
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*

class DetailMainActivity : AppCompatActivity() {
    private var vpAdapter: FragmentStatePagerAdapter? = null
    //테스트용. 삭제할 페이지
    var firebaseAuth: FirebaseAuth? = null
    var storage : FirebaseStorage? = null
    var imgFileName: String? = null
    var imgFileName2: String? = null
    var imgFileName3: String? = null
    // 형석
    lateinit var item : String

    var thisData: PostDTO? = null
    var userData: UserDTO? = null

    lateinit var imageView : ImageView
    lateinit var imageView2 : ImageView
    lateinit var imageView3 : ImageView

    var bundle : Bundle? = null
    lateinit var transaction: FragmentTransaction

    init {
        bundle = Bundle(1)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        // Firebase - Auth, Firestore의 인스턴스 받아오기
        firebaseAuth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val content : TextView = findViewById(R.id.content)
// 잠시 가림        val replyContent : EditText = findViewById(R.id.reply_content)
        var postDTO : PostDTO? = null
//        imgView = findViewById(R.id.imgView) 뷰페이저 때문에 잠시 가림

        //뷰페이저
//        val siViewpager = findViewById<SIViewPager>(R.id.siviewpager)
        imageView = findViewById(R.id.imgView)
        imageView2 = findViewById(R.id.imgView2)
        imageView3 = findViewById(R.id.imgView3)

        // 형석
        item = intent.getStringExtra("data") as String
        var item_split = item.split(" ")
        var item_name = item_split[0]
        var item_modified = item_split[1]

        db.collection("post").whereEqualTo("name", item_name).get().addOnCompleteListener {
            if (it.isSuccessful) {
                // 파이어스토어에서 현재 게시글 정보 조회

                storage = FirebaseStorage.getInstance()
                val storageReference = storage!!.reference
                for (dc in it.result!!.documents.reversed()) {
                    var data = dc.toObject(PostDTO::class.java)
                    if (data!!.modified == item_modified && data.name == item_name) {
                        thisData = data
                        break
                    }
                }

                if (thisData != null) {
                    content.text = thisData!!.content
                    imgFileName = thisData!!.imageOfDetail
                    imgFileName2 = thisData!!.imageOfDetail2
                    imgFileName3 = thisData!!.imageOfDetail3

                    if (imgFileName != null) {

                        storageReference.child("post/" + imgFileName).downloadUrl.addOnSuccessListener {
                            Glide.with(this)
                                .load(it)
                                .into(imageView)
                        }

                    }
                    if (imgFileName2 != null) {
                        imageView2 = findViewById(R.id.imgView2)
                        storageReference.child("post/" + imgFileName2).downloadUrl.addOnSuccessListener {
                            Glide.with(this)
                                .load(it)
                                .into(imageView2)
                        }
                    }
                    if (imgFileName3 != null) {
                        imageView3 = findViewById(R.id.imgView3)
                        storageReference.child("post/" + imgFileName3).downloadUrl.addOnSuccessListener {
                            Glide.with(this)
                                .load(it)
                                .into(imageView3)
                        }
                    }
                }
                // 테스트. 지울 부분. 댓글 띄우는거 될때까지만 놔둘게요
                db.collection("post/" + thisData!!.registered.toString() + "/reply").get().addOnCompleteListener {
                    if (it.isSuccessful) {
                        var replyDTO : ReplyDTO? = null
                        for (document in it.result!!.documents) {
                            replyDTO = document.toObject(ReplyDTO::class.java)
                            break
                        }

                    }
                }
            }
        }

        imageView.setOnClickListener {
            val sharedPreference = getSharedPreferences("detailImage", 0)
            val editor = sharedPreference.edit()
            editor.putString("detailImage", thisData!!.imageOfDetail)
            editor.apply()
            val intent = Intent(this, DetailImage::class.java)
            startActivity(intent)
        }
        imageView2.setOnClickListener {
            val sharedPreference = getSharedPreferences("detailImage", 0)
            val editor = sharedPreference.edit()
            editor.putString("detailImage", thisData!!.imageOfDetail2)
            editor.apply()
            val intent = Intent(this, DetailImage::class.java)
            startActivity(intent)
        }
        imageView3.setOnClickListener {
            val sharedPreference = getSharedPreferences("detailImage", 0)
            val editor = sharedPreference.edit()
            editor.putString("detailImage", thisData!!.imageOfDetail3)
            editor.apply()
            val intent = Intent(this, DetailImage::class.java)
            startActivity(intent)
        }
        // 수정하기
        findViewById<Button>(R.id.update).setOnClickListener {
            val intent = Intent(this, UpdateActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }
        // 삭제하기
        findViewById<Button>(R.id.delete).setOnClickListener {
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle("삭제하시겠습니까?")
            // 확인시 종료 처리 할 리스너
            var listener = DialogInterface.OnClickListener { dialog, i ->
                db.collection("post").document(thisData!!.registered.toString()).delete()
                finish()
            }
            dialog.setPositiveButton("확인", listener)
            dialog.setNegativeButton("취소", null)
            dialog.show()

        }

        // 댓글보기. 일단 액티비티로 해둠
        findViewById<TextView>(R.id.replyList).setOnClickListener {
            val intent = Intent(this, ReplyActivity::class.java)
            intent.putExtra("id", thisData!!.registered)  // reply 액티비티에 값 전달
            startActivity(intent)

        }

    }

    override fun onBackPressed(){
        // 클릭 시 이전 페이지로
        finish()
    }
}
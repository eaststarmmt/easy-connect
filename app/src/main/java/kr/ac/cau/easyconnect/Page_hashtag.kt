package kr.ac.cau.easyconnect

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*
import kotlin.collections.ArrayList

class Page_hashtag : AppCompatActivity() {

    var firebaseAuth: FirebaseAuth? = null
    var db: FirebaseFirestore? = null
    var storage: FirebaseStorage? = null
    lateinit var text : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page_hashtag)

        // 권한과 파이어스토어 데이터베이스 객체 받아옴
        storage = FirebaseStorage.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val button_goback : ImageButton = findViewById(R.id.bt_goback)
        val recycler_view: RecyclerView = findViewById(R.id.recycler_hashtagView)
        var hashtagPageName: TextView = findViewById(R.id.txt_hashtag_page_name)

        recycler_view.adapter = HashtagAdapter()
        recycler_view.layoutManager = GridLayoutManager(this, 3)

        text = intent.getStringExtra("text") as String
        hashtagPageName.setText(text)

        button_goback.setOnClickListener({
            finish()
        })
    }

    inner class HashtagAdapter() : RecyclerView.Adapter<HashtagAdapter.HashtagViewHolder>() {
        // text 변수에 담긴 내용 가지고 해당하는 글들 가져와야 함
        var arrayPostDTO: ArrayList<PostDTO> = arrayListOf()

        init {
            // 초기화! 처음엔 안 띄움 / 원한다면 모든 회원 정보 띄울 수 있음!! for문 주석만 없애면 된다!!
            db!!.collection("post")
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    // userDTO 리스트 초기화
                    arrayPostDTO!!.clear()

                    for (snapshot in querySnapshot!!.documents) {
                        var post = snapshot.toObject(PostDTO::class.java)

                        if(text in post!!.content.toString()){
                            arrayPostDTO!!.add(post!!)
                        }
                    }
                    notifyDataSetChanged()
                }
        }

        // 검색 옵션에 해당하는 userDTO 객체 수 반환
        override fun getItemCount(): Int {
            return arrayPostDTO.size
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HashtagViewHolder {
            val inflatedView =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.hashtag_item, parent, false)
            return HashtagViewHolder(inflatedView)
        }

        override fun onBindViewHolder(holder: HashtagViewHolder, position: Int) {
            val postDTO = arrayPostDTO[position]
            holder.apply {
                bind(postDTO)
            }
        }

        inner class HashtagViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            var view: View = v
            var hashtagLayout: View = view.findViewById(R.id.layout_hashtag_item)
            var hashtagImage: ImageView = view.findViewById(R.id.img_hashtag)
            var hashtagName : TextView = view.findViewById(R.id.txt_hashtag_name)
//            var hashtagText: TextView = view.findViewById(R.id.txt_hashtag)

            var userDTO = UserDTO()
            var content : String? = null

            fun bind(item: PostDTO) {
                // 검색된 계정의 photo 필드를 바탕으로 ImageView에 Glide로 이미지 뷰 띄워줌
                val storageReference = storage!!.reference
                storageReference.child("post/" + item.imageOfDetail).downloadUrl.addOnSuccessListener {
                    Glide.with(hashtagLayout /* context */)
                        .load(it)
                        .into(hashtagImage)
                }

                content = item.content
                val split_contents = content!!.split(" ").toMutableList() as java.util.ArrayList
                if(item.imageOfDetail.isNullOrEmpty()){
                    var content_name : ArrayList<String> = arrayListOf()
                    for (i in 0..1){
                        content_name.add(split_contents.get(i))
                    }
                    val final_content_name = content_name.joinToString(separator = "\n")
                    hashtagName.setText(final_content_name)
                }

                // 해시태그만 출력하는 부분
//                var hashtag_contents : ArrayList<String> = arrayListOf()
//                for (splited_content in split_contents){
//                    if("#" in splited_content){
//                        val split_contents2 = splited_content.split("#").toMutableList() as java.util.ArrayList
//                        hashtag_contents.add("#" + split_contents2[1])
//                    }
//                }
//                for(index in (0..hashtag_contents.size-1)){
//                    if(hashtag_contents[index] == text){
//                        if(index == 0){
//                            break
//                        }else{
//                            Collections.swap(hashtag_contents, 0, index)
//                            break
//                        }
//                    }
//                }
//                val final_hashtag_contents = hashtag_contents.joinToString(separator = "  ")
//
//                hashtagText.setText(final_hashtag_contents)

                // 게시 글을 클릭 했을 때!! 게시 글 페이지로 이동해야 한다.

                hashtagLayout.setOnClickListener{
                    val intentDetail = Intent(view.context, DetailMainActivity::class.java).apply{
                        val data = item.name + " " + item.modified
                        putExtra("data", data)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    startActivity(intentDetail)
                }
            }
        }
    }

    override fun onBackPressed() {
        finish()
    }
}
package kr.ac.cau.easyconnect

import android.app.Activity
import android.content.Intent
import android.graphics.ImageDecoder
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class Page_listOfFan : AppCompatActivity() {

    var firebaseAuth : FirebaseAuth? = null
    var storage : FirebaseStorage? = null
    var db : FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page_list_of_fan)

        // 권한과 파이어스토어 데이터베이스 객체 받아옴
        storage = FirebaseStorage.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val recycler_view: RecyclerView = findViewById(R.id.recycler_fan)
        val button_goback : ImageButton = findViewById(R.id.bt_goback)

        recycler_view.adapter = FriendAdapter()
        recycler_view.layoutManager = LinearLayoutManager(this)

        button_goback.setOnClickListener({
            val intentMypage = Intent(this, Page_mypage::class.java)
            startActivity(intentMypage)
            finish()
        })
    }

    inner class FriendAdapter() : RecyclerView.Adapter<FriendAdapter.FriendViewHolder>() {
        // 띄워줄 정보에 해당하는 UserDTO 객체들을 담을 리스트
        var arrayUserDTO: ArrayList<UserDTO> = arrayListOf()

        init {
            // 자신과 친구인 사람만 띄워야 함!! << 중요할 듯?
            var myDTO = UserDTO()
            // 초기화! 처음엔 안 띄움 / 원한다면 모든 회원 정보 띄울 수 있음!! for문 주석만 없애면 된다!!
            db!!.collection("user_information").whereEqualTo("email", firebaseAuth!!.currentUser.email).get().addOnCompleteListener {
                if (it.isSuccessful) {
                    for (dc in it.result!!.documents) {
                        myDTO = dc.toObject(UserDTO::class.java)!!
                        break
                    }
                    val myFollower = myDTO.followed!!.split(",").toMutableList() as ArrayList


                    db!!.collection("user_information")
                        .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                            // userDTO 리스트 초기화
                            arrayUserDTO!!.clear()

                            for (snapshot in querySnapshot!!.documents) {
                                var user = snapshot.toObject(UserDTO::class.java)
                                if(user!!.email != myDTO.email){
                                    // 자신의 정보는 출력할 필요가 없으므로 추가하지 않음
                                    if(user!!.search == false){
                                        // 검색 불허
                                    }else{
                                        for (email in myFollower)
                                        if(user!!.email == email){
                                            arrayUserDTO!!.add(user!!)
                                        }
                                    }
                                }
                            }
                            notifyDataSetChanged()
                        }
                }
            }
        }

        // 검색 옵션에 해당하는 userDTO 객체 수 반환
        override fun getItemCount(): Int {
            return arrayUserDTO.size
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
            val inflatedView =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_of_fan_item, parent, false)
            return FriendViewHolder(inflatedView)
        }

        override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
            val userDTO = arrayUserDTO[position]
            holder.apply {
                bind(userDTO)
            }
        }

        // friend_item_list.xml 파일 view에 연결해서 동작
        inner class FriendViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            var view: View = v
            var fanLayout: View = view.findViewById(R.id.layout_fan_item)
            var fanImage: ImageView = view.findViewById(R.id.img_fan)
            var fanName: TextView = view.findViewById(R.id.txt_fanname)

            fun bind(item: UserDTO) {
                // 검색된 계정의 photo 필드를 바탕으로 ImageView에 Glide로 이미지 뷰 띄워줌
                val storageReference = storage!!.reference
                storageReference.child("user_profile/" + item.photo).downloadUrl.addOnSuccessListener {
                    Glide.with(fanLayout /* context */)
                        .load(it)
                        .into(fanImage)
                }
                fanName.setText(item.name)

                // 이미지 배경 동그랗게 !
                fanImage.setBackground(ShapeDrawable(OvalShape()))
                fanImage.setClipToOutline(true)

                fanLayout.setOnClickListener({
                    val intentFriendPage = Intent(view.context, Page_friendpage::class.java).apply{
                        val data = item.email
                        val flag = "find"
                        putExtra("friendEmail", data)
                        putExtra("flag", flag)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    startActivity(intentFriendPage)
                })
            }
        }
    }

    override fun onBackPressed(){
        val intentMypage = Intent(this, Page_mypage::class.java)
        startActivity(intentMypage)
        finish()
    }
}
package kr.ac.cau.easyconnect

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage

class Page_imagefriend : AppCompatActivity() {
    var storage : FirebaseStorage? = null
    lateinit var  imageView_friend : ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page_imagefriend)

        imageView_friend = findViewById(R.id.img_friend_expanded)
        val button_goback : ImageButton = findViewById(R.id.bt_goback)


        button_goback.setOnClickListener({
            val intentMypage = Intent(this, Page_mypage::class.java)
            startActivity(intentMypage)
            finish()
        })
    }

    override fun onBackPressed(){
        // 클릭 시 이전 페이지인 메인으로!
        val intentFriendpage = Intent(this, Page_friendpage::class.java)
        startActivity(intentFriendpage)
        finish()
    }
    fun receiveData(userDTO : UserDTO){
        storage = FirebaseStorage.getInstance()
        val storageReference = storage!!.reference

        storageReference.child("user_profile/" + userDTO.photo).downloadUrl.addOnSuccessListener {
            Glide.with(this /* context */)
                .load(it)
                .into(imageView_friend)
        }
    }
}
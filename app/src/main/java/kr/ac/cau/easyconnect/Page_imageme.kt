package kr.ac.cau.easyconnect

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage

class Page_imageme : AppCompatActivity() {

    var storage : FirebaseStorage? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page_imageme)

        var imageView_me : ImageView = findViewById(R.id.img_me_expanded)
        val button_goback : ImageButton = findViewById(R.id.bt_goback)
        var photoname : String? = null

        storage = FirebaseStorage.getInstance()
        val storageReference = storage!!.reference

        val sharedPreference = getSharedPreferences("imageme", 0)
        photoname = sharedPreference.getString("image", "")

        storageReference.child("user_profile/" + photoname).downloadUrl.addOnSuccessListener {
            Glide.with(this /* context */)
                .load(it)
                .into(imageView_me)
        }

        button_goback.setOnClickListener({
            val intentMypage = Intent(this, Page_mypage::class.java)
            startActivity(intentMypage)
            finish()
        })
    }

    override fun onBackPressed(){
        // 클릭 시 이전 페이지인 메인으로!
        val intentMypage = Intent(this, Page_mypage::class.java)
        startActivity(intentMypage)
        finish()
    }
}
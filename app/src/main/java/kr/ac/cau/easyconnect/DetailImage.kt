package kr.ac.cau.easyconnect

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.ImageButton
import android.widget.ImageView
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import java.lang.Float.max
import java.lang.Float.min

class DetailImage : AppCompatActivity() {

    var storage : FirebaseStorage? = null
    private var scaleGestureDetector: ScaleGestureDetector? = null
    private var scaleFactor = 1.0f
    var imgView : ImageView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_image)

        val imageView : ImageView = findViewById(R.id.imageView)
        val button_goback : ImageButton = findViewById(R.id.bt_goback)
        var imageName : String? = null
        imgView = findViewById(R.id.imageView)
        storage = FirebaseStorage.getInstance()
        val storageReference = storage!!.reference

        val sharedPreference = getSharedPreferences("detailImage", 0)
        imageName = sharedPreference.getString("detailImage", "")

        storageReference.child("post/" + imageName).downloadUrl.addOnSuccessListener {
            Glide.with(this /* context */)
                .load(it)
                .into(imageView)
        }

        button_goback.setOnClickListener({
            finish()
        })
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        scaleGestureDetector?.onTouchEvent(event)
        return true
    }

    inner class ScaleListner : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        @RequiresApi(Build.VERSION_CODES.N)
        override fun onScale(detector: ScaleGestureDetector?): Boolean {
            scaleFactor *= scaleGestureDetector!!.scaleFactor

            // 최소 0.5, 최대 2배
            scaleFactor = max(0.5f, min(scaleFactor, 2.0f))
            imgView!!.scaleX = scaleFactor
            imgView!!.scaleY = scaleFactor
            return true
        }
    }

    override fun onBackPressed(){
        // 클릭 시 이전 페이지인 메인으로!
        finish()
    }
}
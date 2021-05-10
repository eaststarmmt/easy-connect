package kr.ac.cau.easyconnect

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView

class CustomLodingDialog(context: Context):Dialog(context) {
    lateinit var turnHorizontal : Animation
    lateinit var turnAround : Animation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.custom_loading_dialog)

        turnHorizontal = AnimationUtils.loadAnimation(context, R.anim.turn_horizontal)
        turnAround = AnimationUtils.loadAnimation(context, R.anim.turn_around)
/*
        val loading_img : ImageView = findViewById<ImageView>(R.id.loading_img)
        val loading_img2 : ImageView = findViewById<ImageView>(R.id.loading_img2)
        loading_img.startAnimation(turnHorizontal)
        loading_img2.startAnimation(turnAround)

 */
//    val loading_img : ImageView = findViewById<ImageView>(R.id.loading_img)
//        loading_img.startAnimation(turnAround)
    }
}
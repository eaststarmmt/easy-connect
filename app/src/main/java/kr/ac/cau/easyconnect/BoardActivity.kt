package kr.ac.cau.easyconnect

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class BoardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_board)
        findViewById<Button>(R.id.create).setOnClickListener{
            var intent = Intent(this, PostActivity::class.java)
            startActivity(intent)
        }
    }
}
package kr.ac.cau.easyconnect

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth

class Page_signup2 : AppCompatActivity() {

    private val exist_id = "leehs"
    private var firebaseAuth: FirebaseAuth? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page_signup2)

        var button_fin_signup : Button = findViewById(R.id.bt_fin_signup)
        var editText_check_id : EditText = findViewById(R.id.edit_check_id)
        var editText_check_password : EditText = findViewById(R.id.edit_check_password)
        var editText_check_password2 : EditText = findViewById(R.id.edit_check_password2)

        firebaseAuth = FirebaseAuth.getInstance()

        button_fin_signup.setOnClickListener({
            var input_password = editText_check_password.text.trim().toString()
            var input_password2 = editText_check_password2.text.trim().toString()
            var input_id = editText_check_id.text.trim().toString()

            if(input_password.isNullOrEmpty() || input_password2.isNullOrEmpty()){
                var builder = AlertDialog.Builder(this)
                builder.setTitle("입력 정보가 누락되었습니다.")
                builder.setIcon(R.mipmap.ic_launcher)
                builder.setPositiveButton("확인", null)
                builder.show()
            }
            else if(input_password != input_password2){
                var builder = AlertDialog.Builder(this)
                builder.setTitle("입력된 두 비밀번호가 다릅니다.")
                builder.setIcon(R.mipmap.ic_launcher)
                builder.setPositiveButton("확인", null)
                builder.show()
                editText_check_password.setText("")
                editText_check_password2.setText("")
            }
            else{
                firebaseAuth!!.createUserWithEmailAndPassword(input_id, input_password)
                    .addOnCompleteListener(this){
                        if (it.isSuccessful){
                            val user = firebaseAuth?.currentUser
                            Toast.makeText(this, "Authentication success.", Toast.LENGTH_SHORT).show()
                            val intentLogin = Intent(this, Page_login::class.java)
                            startActivity(intentLogin)
                        }else{
                            Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                            var builder = AlertDialog.Builder(this)
                            builder.setTitle(input_id + ", " + input_password)
                            builder.setIcon(R.mipmap.ic_launcher)
                            builder.setPositiveButton("확인", null)
                            builder.show()

                            // 이메일 다시 입력
                            editText_check_id.setText("")
                        }
                    }
            }

        })

    }

}
package kr.ac.cau.easyconnect

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User
import org.w3c.dom.Text

class Page_mypage : AppCompatActivity() {
    // 마이페이지 구현
    var firebaseAuth : FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page_mypage)

        firebaseAuth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        val button_change_img : Button = findViewById(R.id.bt_change_img)
        val button_change_password : Button = findViewById(R.id.bt_change_password)
        val button_withdrawal : Button = findViewById(R.id.bt_withdrawal)

        // 현재 정보를 기본적으로 출력하는 걸 구현해야 함 2021-04-15 04:39 !!!!!!!!!!!!!!!!!!!!!!! 아직 미구현

        button_change_img.setOnClickListener({
            // 이미지 변경 기능 2021-04-15 04:39 !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! 아직 미구현
        })

        button_change_password.setOnClickListener({
            // 비밀번호 변경 기능 완료

            var builder = AlertDialog.Builder(this)
            builder.setView(layoutInflater.inflate(R.layout.update_password_dialog, null))

            var listener = DialogInterface.OnClickListener{p0, _->
                var dialog = p0 as AlertDialog
                var input_password : EditText? = dialog.findViewById(R.id.edit_change_password)

                firebaseAuth!!.currentUser.updatePassword(input_password!!.text.toString()).addOnCompleteListener(this){
                    if(it.isSuccessful){
                        // 비밀번호 변경 성공
                        Toast.makeText(this, "비밀번호 변경 성공!!", Toast.LENGTH_SHORT).show()
                        // 파이어스토어 변경
                        var userDTO : UserDTO? = null
                        var userPhoneNumber : String

                        // 현재 유저의 아이디(이메일 정보)를 기반으로 파이어스토어 접근
                        db.collection("user_information").whereEqualTo("email", firebaseAuth!!.currentUser.email).get().addOnCompleteListener{
                            if(it.isSuccessful){
                                for(dc in it.result!!.documents){
                                    userDTO = dc.toObject(UserDTO::class.java)
                                    break
                                }
                                if(userDTO != null){
                                    // 파이어스토어 업데이트!!
                                    userDTO!!.password = input_password!!.text.toString()
                                    userPhoneNumber = userDTO!!.phoneNumber.toString()

                                    // 파이어스토어의 현재 회원 정보 삭제 및 추가 (업데이트)
                                    db.collection("user_information").document(userPhoneNumber).delete()
                                    db.collection("user_information").document(userPhoneNumber).set(userDTO!!)
                                }
                            }
                        }
                    }
                }
            }
            builder.setPositiveButton("변경", listener)
            builder.setNegativeButton("취소", null)
            builder.show()
        })

        button_withdrawal.setOnClickListener({
            // 회원 탈퇴 기능 완료
            var builder = AlertDialog.Builder(this)
            builder.setTitle("정말로 탈퇴하시겠습니까?")
            builder.setIcon(R.mipmap.ic_launcher)

            var listener = DialogInterface.OnClickListener { p0, _ ->
                /////////////////////// 데이터베이스 firestore에서도 해당 회원 정보 삭제해야함!!!!!!!!!!!!!!!!!! 2021-04-15 04:39
                var userDTO : UserDTO? = null
                var userPhoneNumber : String
                db.collection("user_information").whereEqualTo("email", firebaseAuth!!.currentUser.email).get().addOnCompleteListener{
                    if(it.isSuccessful){
                        for(dc in it.result!!.documents){
                            userDTO = dc.toObject(UserDTO::class.java)
                            break
                        }
                        if(userDTO != null){
                            userPhoneNumber = userDTO!!.phoneNumber.toString()
                            // 파이어스토어의 현재 회원 정보 삭제
                            db.collection("user_information").document(userPhoneNumber).delete()

                            // 현재 로그인 정보 삭제
                            firebaseAuth!!.currentUser.delete()
                        }
                    }
                }

                val intentLogin = Intent(this, Page_login::class.java)
                startActivity(intentLogin)
                finish()
            }

            builder.setPositiveButton("확인", listener)
            builder.setNegativeButton("취소", null)
            builder.show()
        })
    }
}
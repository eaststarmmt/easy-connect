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
                var input_change_password : EditText? = dialog.findViewById(R.id.edit_change_password)
                var input_current_password : EditText? = dialog.findViewById(R.id.edit_current_password)

                db.collection("user_information").whereEqualTo("email", firebaseAuth!!.currentUser.email).get().addOnCompleteListener{
                    if(it.isSuccessful){
                        // 현재 로그인 정보의 파이어스토어 데이터 조회
                        var userDTO : UserDTO? = null
                        for(dc in it.result!!.documents){
                            userDTO = dc.toObject(UserDTO::class.java)
                            break
                        }
                        if(userDTO != null){
                            // 조회 성공 시
                            if(input_current_password!!.text.toString() == userDTO!!.password){
                                // 현재 비밀번호가 접속중인 로그인 정보와 일치하는지 비교
                                    // 일치한다면
                                firebaseAuth!!.currentUser.updatePassword(input_change_password!!.text.toString()).addOnCompleteListener(this){
                                    if(it.isSuccessful) {
                                        // 비밀번호 변경 성공
                                        Toast.makeText(this, "비밀번호 변경 성공!!", Toast.LENGTH_SHORT).show()
                                        // 파이어스토어 변경
                                        var userPhoneNumber: String

                                        if (userDTO != null) {
                                            // 파이어스토어 업데이트!!
                                            userDTO!!.password = input_change_password!!.text.toString()
                                            userPhoneNumber = userDTO!!.phoneNumber.toString()

                                            // 파이어스토어의 현재 회원 정보 삭제 및 추가 (업데이트)
                                            db.collection("user_information").document(userPhoneNumber).delete()
                                            db.collection("user_information").document(userPhoneNumber).set(userDTO!!)
                                        }
                                    }
                                }
                            }else{
                                // 로그인 정보와 일치하지 않는다면
                                Toast.makeText(this, "변경 실패! 현재 비밀번호를 틀렸습니다.", Toast.LENGTH_SHORT).show()
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
            builder.setView(layoutInflater.inflate(R.layout.withdrawal_dialog, null))

            var listener = DialogInterface.OnClickListener { p0, _ ->
                /////////////////////// 데이터베이스 firestore에서도 해당 회원 정보 삭제해야함!!!!!!!!!!!!!!!!!! 2021-04-15 04:39
                var dialog = p0 as AlertDialog
                var input_current_password2 : EditText? = dialog.findViewById(R.id.edit_current_password2)

                var userDTO : UserDTO? = null
                var userPhoneNumber : String
                db.collection("user_information").whereEqualTo("email", firebaseAuth!!.currentUser.email).get().addOnCompleteListener{
                    if(it.isSuccessful){
                        for(dc in it.result!!.documents){
                            userDTO = dc.toObject(UserDTO::class.java)
                            break
                        }
                        if(userDTO != null){
                            if(input_current_password2!!.text.toString() == userDTO!!.password){
                                // 비밀번호 일치
                                userPhoneNumber = userDTO!!.phoneNumber.toString()
                                // 파이어스토어의 현재 회원 정보 삭제
                                db.collection("user_information").document(userPhoneNumber).delete()

                                // 현재 로그인 정보 삭제
                                firebaseAuth!!.currentUser.delete()
                                Toast.makeText(this, "회원 탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT).show()

                                val intentLogin = Intent(this, Page_login::class.java)
                                startActivity(intentLogin)
                                finish()
                            }else{
                                Toast.makeText(this, "비밀번호가 틀렸습니다.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }

            builder.setPositiveButton("확인", listener)
            builder.setNegativeButton("취소", null)
            builder.show()
        })
    }
}
package kr.ac.cau.easyconnect

import android.content.Intent
import android.os.Bundle
import android.widget.CompoundButton
import android.widget.ImageButton
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


// 구현 할지 말지 미정

class Page_sets : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page_sets)

        val button_goback : ImageButton = findViewById(R.id.bt_goback)
        val switchSearch : Switch = findViewById(R.id.switch_search)

        button_goback.setOnClickListener({
            val intentMenu = Intent(this, Page_menu::class.java)
            startActivity(intentMenu)
            finish()
        })

        // 셋팅 부분 구현해야함!!           알림 키고 끄기 / 폰트크기 / 본인 정보 검색 가능하게 할 건지?
        val sharedPreference = getSharedPreferences("searchBoolean", 0)
        switchSearch.isChecked = sharedPreference.getBoolean("searchState", false)

        switchSearch.setOnCheckedChangeListener(searchSwitchListener())
    }

    inner class searchSwitchListener : CompoundButton.OnCheckedChangeListener {

        override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
            var firebaseAuth = FirebaseAuth.getInstance()
            var db = FirebaseFirestore.getInstance()
            var userDTO : UserDTO? = null

            db.collection("user_information").whereEqualTo("email", firebaseAuth.currentUser.email).get()
                    .addOnCompleteListener{
                        if(it.isSuccessful){
                            for (dc in it.result!!.documents) {
                                userDTO = dc.toObject(UserDTO::class.java)
                                break
                            }
                        }

                        if (isChecked){
                            if (userDTO != null) {
                                userDTO!!.search = true
                                val sharedPreference = getSharedPreferences("searchBoolean", 0)
                                val editor = sharedPreference.edit()
                                editor.putBoolean("searchState", userDTO!!.search!!)
                                editor.apply()
                            }
                        }
                        else{
                            if (userDTO != null) {
                                userDTO!!.search = false
                                val sharedPreference = getSharedPreferences("searchBoolean", 0)
                                val editor = sharedPreference.edit()
                                editor.putBoolean("searchState", userDTO!!.search!!)
                                editor.apply()
                            }
                        }
                        // 파이어스토어 갱신
                        db.collection("user_information")
                                .document(userDTO!!.uid.toString()).delete()
                        db.collection("user_information")
                                .document(userDTO!!.uid.toString()).set(
                                        userDTO!!
                                )
                    }

        }
    }

    override fun onBackPressed(){
        // 클릭 시 이전 페이지인 메인으로!
        val intentMenu = Intent(this, Page_menu::class.java)
        startActivity(intentMenu)
        finish()
    }

}
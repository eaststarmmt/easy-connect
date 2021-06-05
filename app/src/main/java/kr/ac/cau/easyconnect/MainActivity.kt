package kr.ac.cau.easyconnect

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    var firebaseAuth = FirebaseAuth.getInstance()
    var db = FirebaseFirestore.getInstance()
    var userDTO : UserDTO = UserDTO()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button_menu : Button = findViewById(R.id.bt_menu)
        val button_post : Button = findViewById(R.id.bt_post)
        val pageAdapter = PagerAdapter(supportFragmentManager, 1)
        val fragmentList = arrayListOf(Timeline(), Friends(), Recommendation())

        pageAdapter.fragmentList = fragmentList

        var viewPager : ViewPager = findViewById(R.id.viewpager)
        viewPager.adapter = pageAdapter

        val tabLayout : TabLayout = findViewById(R.id.tab_layout)
        tabLayout.setupWithViewPager(viewPager)

        var arrayUserDTO: java.util.ArrayList<UserDTO> = arrayListOf()
        var arrayRecommendUserDTO: java.util.ArrayList<UserDTO> = arrayListOf()

        db!!.collection("user_information")
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                // userDTO 리스트 초기화
                arrayUserDTO!!.clear()
                arrayRecommendUserDTO!!.clear()

                for (snapshot in querySnapshot!!.documents) {
                    var user = snapshot.toObject(UserDTO::class.java)
                    if (user!!.email != firebaseAuth!!.currentUser.email) {
                        // 자신의 정보는 출력할 필요가 없으므로 추가하지 않음
                        if (user!!.search == false) {
                            // 검색 불허
                        } else {
                            arrayUserDTO!!.add(user!!)
                        }
                    }
                }
                arrayUserDTO.sortByDescending { it.followerCount }

                for (index in 0..4) {
                    arrayRecommendUserDTO.add(arrayUserDTO.get(index))
                }

                Collections.shuffle(arrayRecommendUserDTO)

                val bundle = Bundle()
                bundle.putString("photo1", arrayRecommendUserDTO.get(0).photo)
                bundle.putString("photo2", arrayRecommendUserDTO.get(1).photo)
                bundle.putString("photo3", arrayRecommendUserDTO.get(2).photo)
                bundle.putString("name1", arrayRecommendUserDTO.get(0).name)
                bundle.putString("name2", arrayRecommendUserDTO.get(1).name)
                bundle.putString("name3", arrayRecommendUserDTO.get(2).name)
                bundle.putString("email1", arrayRecommendUserDTO.get(0).email)
                bundle.putString("email2", arrayRecommendUserDTO.get(1).email)
                bundle.putString("email3", arrayRecommendUserDTO.get(2).email)
                fragmentList[2].arguments = bundle
            }

        // 환경 설정부분 (타인의 검색을 허용할 지에 대한 공유정보)
        db.collection("user_information").whereEqualTo("email", firebaseAuth.currentUser.email).get()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        for (dc in it.result!!.documents) {
                            userDTO = dc.toObject(UserDTO::class.java)!!
                            break
                        }
                        val sharedPreference = getSharedPreferences("searchBoolean", 0)
                        val editor = sharedPreference.edit()
                        editor.putBoolean("searchState", userDTO!!.search!!)
                        editor.apply()
                    }
                }

        button_menu.setOnClickListener({
            val intentMenu = Intent(this, Page_menu::class.java)
            startActivity(intentMenu)
            finish()
        })

        button_post.setOnClickListener{
            val intent = Intent(this, WriteActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    inner class PagerAdapter(fm: FragmentManager, behavior: Int) :
        FragmentPagerAdapter(fm, behavior)
    {
        var fragmentList = ArrayList<Fragment>()

        override fun getItem(position: Int): Fragment = fragmentList[position]

        override fun getCount(): Int = fragmentList.size

        override fun getPageTitle(position: Int): CharSequence? {
            return when(position) {
                0-> "나의 이야기"
                1-> "지켜봐요"
                else-> "추천해요"
            }
        }
    }

    override fun onBackPressed(){
        // 회원탈퇴 후 뒤로가는 기능 막기 위함
        // 클릭 시 종료 여부 체크하고 종료 버튼 누르면 앱 종료
        var builder_dialog = AlertDialog.Builder(this);
        builder_dialog.setTitle("종료할까요?"); // 다이얼로그 제목
        builder_dialog.setIcon(R.mipmap.easy_connect)
        var listener = DialogInterface.OnClickListener { dialog, which
            -> ActivityCompat.finishAffinity(this)
            System.exit(0)
        }
        builder_dialog.setPositiveButton("종료", listener)
        builder_dialog.setNegativeButton("취소", null)
        builder_dialog.show(); // 다이얼로그 보이기
    }

}
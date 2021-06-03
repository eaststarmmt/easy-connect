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
        })

        button_post.setOnClickListener{
            val intent = Intent(this, WriteActivity::class.java)
            startActivity(intent)
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
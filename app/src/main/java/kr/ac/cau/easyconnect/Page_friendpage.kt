package kr.ac.cau.easyconnect

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class Page_friendpage : AppCompatActivity() {
    // 마이페이지 구현

    var firebaseAuth : FirebaseAuth? = null
    var storage : FirebaseStorage? = null
    var db : FirebaseFirestore? = null

    var flag : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page_friendpage)

        // 권한과 파이어스토어 데이터베이스 객체 받아옴
        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // xml 파일의 버튼과 텍스트, 그리고 이미지뷰에 연결!
        val button_home: Button = findViewById(R.id.bt_home)
        val pageAdapter = PagerAdapter(supportFragmentManager, 1)
        val fragmentList = arrayListOf(Timeline_friend(), Friendpage())

        val friendEmail = intent.getStringExtra("friendEmail") as String
        flag = intent.getStringExtra("flag") as String

        val bundle = Bundle()
        bundle.putString("email", friendEmail)
        fragmentList[0].arguments = bundle
        fragmentList[1].arguments = bundle

        pageAdapter.fragmentList = fragmentList

        var viewPager : ViewPager = findViewById(R.id.viewpager)
        viewPager.adapter = pageAdapter

        val tabLayout : TabLayout = findViewById(R.id.tab_layout)
        tabLayout.setupWithViewPager(viewPager)

        button_home.setOnClickListener({
            val intentHome = Intent(this, MainActivity::class.java)
            startActivity(intentHome)
            finish()
        })
    }

    inner class PagerAdapter(fm: FragmentManager, behavior: Int) :
        FragmentPagerAdapter(fm, behavior)
    {
        var fragmentList = ArrayList<Fragment>()

        override fun getItem(position: Int): Fragment = fragmentList[position]

        override fun getCount(): Int = fragmentList.size

        override fun getPageTitle(position: Int): CharSequence? {
            return when(position) {
                0-> "친구 이야기"
                else-> "친구 정보"
            }
        }
    }



    override fun onBackPressed(){
        // 이전 페이지로 돌아가야함
        if(flag == "find"){
            finish()
        }else{
//            val intentMain = Intent(this, MainActivity::class.java)
//            startActivity(intentMain)
            finish()
        }
    }
}
package kr.ac.cau.easyconnect

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button_menu : Button = findViewById(R.id.bt_menu)
        val pageAdapter = PagerAdapter(supportFragmentManager, 1)
        val fragmentList = arrayListOf(Timeline(), Friends(), Recommendation())

        pageAdapter.fragmentList = fragmentList

        var viewPager : ViewPager = findViewById(R.id.viewpager)
        viewPager.adapter = pageAdapter

        val tabLayout : TabLayout = findViewById(R.id.tab_layout)
        tabLayout.setupWithViewPager(viewPager)

        // 환경 설정부분

        var builder_dialog = AlertDialog.Builder(this);
        builder_dialog.setTitle("잘 안보인다면 글자 크기를 변경하세요!"); // 다이얼로그 제목
        builder_dialog.setPositiveButton("확인", null)
        builder_dialog.show(); // 다이얼로그 보이기


        button_menu.setOnClickListener({
            val intentMenu = Intent(this, Page_menu::class.java)
            startActivity(intentMenu)
            finish()
        })
        findViewById<Button>(R.id.bt_post).setOnClickListener{
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
                0-> "타임라인"
                1-> "친구들"
                else-> "추천 키워드"
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
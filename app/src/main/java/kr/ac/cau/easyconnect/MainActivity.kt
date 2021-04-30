package kr.ac.cau.easyconnect

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.system.Os.bind
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.widget.Button
import android.widget.ImageView
import android.widget.TabHost
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide.init
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.FieldPosition

// 미완 아직 개발 단계 아님

class MainActivity : AppCompatActivity() {

    var firebaseAuth: FirebaseAuth? = null
    var db: FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val button_menu : Button = findViewById(R.id.bt_menu)

        // 메인 뷰 (자신의 글을 포토카드 형식으로 출력) / 마지막에 게시글 추가 버튼 !!!!!!!!!!!!
        val host : TabHost = findViewById(R.id.host)
        host.setup()

        val tabSpec1 : TabHost.TabSpec = host.newTabSpec("Tab Spec 1")
        tabSpec1.setContent(R.id.content1)
        tabSpec1.setIndicator("타임라인")
        host.addTab(tabSpec1)

        val tabSpec2 : TabHost.TabSpec = host.newTabSpec("Tab Spec 2")
        tabSpec2.setContent(R.id.content2)
        tabSpec2.setIndicator("친구들")
        host.addTab(tabSpec2)

        val tabSpec3 : TabHost.TabSpec = host.newTabSpec("Tab Spec 3")
        tabSpec3.setContent(R.id.content3)
        tabSpec3.setIndicator("추천 키워드")
        host.addTab(tabSpec3)

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

/*  현재 작성중인 코드!
    // post 관련 데이터베이스가 없어서 아직 미정입니다.

    inner class PhotoCardAdapter() : RecyclerView.Adapter<PhotoCardAdapter.PhotoCardViewHolder>(){
        // 로그인 한 유저의 작성 글 정보를 가져와서 배열에 담아두기 위한 것
        var arrayPostDTO : ArrayList<PostDTO> = arrayListOf()

        init{
            // 파이어스토어에서 작성 글에 관한 데이터베이스 정보를 가져온다.
            // 전부 띄워줘야 하므로 받아와서 arrayPostDTO 에 저장할 것
            db!!.collection('post_information').addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                arrayPostDTO.clear()

                // post_information에 있는 데이터 정보를 가져와서 각 정보마다 현재 로그인 한 유저의 글인지 비교해야함
                for (snapshot in querySnapshot!!.documents) {
                    var post = snapshot.toObject(PostDTO::class.java)

                    // 이름으로 비교해야 할 듯? 아니면 현재 유저의 이메일 정보로 이름 찾아와서 비교하던지..
                    if (post!!.name == firebaseAuth.currentUser.name) {
                        // 자신의 정보와 일치하는 글만 가져온다!
                        arrayPostDTO!!.add(post!!)
                    }
                }
                notifyDataSetChanged()
            }
        }

        override fun getItemCount(): Int{
            return arrayPostDTO.size
        }

        override fun onCreateViewHolder(parent : ViewGroup, viewType : Int) : PhotoCardViewHolder{
            val inflatedView =
                    LayoutInflater.from(parent.context)
                            .inflate(R.layout.photocard_item_list, parent, false)
            return PhotoCardViewHolder(inflatedView)
        }

        override fun onBindViewHolder(holder: PhotoCardViewHolder, position: Int){
            val postDTO = arrayPostDTO[position]
            holder.apply{
                bind(postDTO)
            }
        }

        inner class PhotoCardViewHolder(v: View) : RecyclerView.ViewHolder(v){
            var view : View = v
            var photoCardLayout: View = view.findViewById(R.id.layout_photoCard_item)
            var photoOfDetail: ImageView = view.findViewById(R.id.photo_of_post)
            var nameOfDetail: TextView = view.findViewById(R.id.name_of_post)

            fun bind(item : PostDTO){

            }
        }
    }

*/

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
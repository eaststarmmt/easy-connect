package kr.ac.cau.easyconnect

import android.content.Context
import android.content.Intent
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.time.LocalDateTime

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
class Friends : Fragment() {
    // TODO: Rename and change types of parameters

    var storage: FirebaseStorage? = null
    var firebaseAuth: FirebaseAuth? = null
    var db: FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        storage = FirebaseStorage.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view : View = inflater.inflate(R.layout.fragment_friends, container, false)
        val timelineView : RecyclerView = view.findViewById(R.id.recycler_friendlist)
        val context: Context = view.context
        val lm = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        timelineView.layoutManager = lm
        timelineView.adapter = FriendAdapter()

        return view
    }

    @RequiresApi(Build.VERSION_CODES.O)
    inner class FriendAdapter() : RecyclerView.Adapter<FriendAdapter.FriendViewHolder>() {
        // 띄워줄 정보에 해당하는 UserDTO 객체들을 담을 리스트
        var arrayUserDTO: ArrayList<UserDTO> = arrayListOf()

        init {
            // 자신과 친구인 사람만 띄워야 함!! << 중요할 듯?
            var myDTO = UserDTO()
            // 초기화! 처음엔 안 띄움 / 원한다면 모든 회원 정보 띄울 수 있음!! for문 주석만 없애면 된다!!
            db!!.collection("user_information").whereEqualTo("email", firebaseAuth!!.currentUser.email).get().addOnCompleteListener {
                if (it.isSuccessful) {
                    for (dc in it.result!!.documents) {
                        myDTO = dc.toObject(UserDTO::class.java)!!
                        break
                    }
                    val myFollower = myDTO.following!!.split(",").toMutableList() as ArrayList

                    db!!.collection("user_information")
                        .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                            // userDTO 리스트 초기화
                            arrayUserDTO!!.clear()

                            for (snapshot in querySnapshot!!.documents) {
                                var user = snapshot.toObject(UserDTO::class.java)
                                if(user!!.email != myDTO.email){
                                    // 자신의 정보는 출력할 필요가 없으므로 추가하지 않음
                                    if(user!!.search == false){
                                        // 검색 불허
                                    }else{
                                        for (email in myFollower)
                                            if(user!!.email == email){
                                                arrayUserDTO!!.add(user!!)
                                            }
                                    }
                                }
                            }
                            notifyDataSetChanged()
                        }
                }
            }
        }

        // 검색 옵션에 해당하는 userDTO 객체 수 반환
        override fun getItemCount(): Int {
            return arrayUserDTO.size
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
            val inflatedView =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.friend_item_list, parent, false)
            return FriendViewHolder(inflatedView)
        }

        override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
            val userDTO = arrayUserDTO[position]
            holder.apply {
                bind(userDTO)
            }
        }

        // friend_item_list.xml 파일 view에 연결해서 동작
        inner class FriendViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            var view: View = v
            var friendLayout: View = view.findViewById(R.id.layout_friend_item)
            var friendImage: ImageView = view.findViewById(R.id.img_friend)
            var friendName: TextView = view.findViewById(R.id.txt_friendName)
            var friendNewImage : ImageView = view.findViewById(R.id.img_new_friend)

            var flag = 0

            fun bind(item: UserDTO) {
                if(flag == 0){
                    var postDTO = PostDTO()
                    db!!.collection("post")
                        .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                            for (snapshot in querySnapshot!!.documents.reversed()) {
                                if (snapshot.toObject(PostDTO::class.java)!!.name == item.email) {
                                    postDTO = snapshot.toObject(PostDTO::class.java)!!
                                    break
                                }
                            }

                            var now: String = LocalDateTime.now().toString()
                            var lastDate: String? = postDTO!!.modified
                            if(!postDTO!!.modified.isNullOrEmpty()){
                                var parsing_now = now.split("T")
                                var parsing_now_date = parsing_now[0].split("-")

                                var parsing_lastDate = lastDate!!.split("T")
                                var parsing_lastDate_date =
                                    parsing_lastDate[0].split("-")

                                var detailDateFromNow_year: Int =
                                    parsing_now_date[0].toInt() - parsing_lastDate_date[0].toInt()
                                var detailDateFromNow_month: Int =
                                    parsing_now_date[1].toInt() - parsing_lastDate_date[1].toInt()
                                var detailDateFromNow_day: Int =
                                    parsing_now_date[2].toInt() - parsing_lastDate_date[2].toInt()

                                // 날짜 수정
                                if (detailDateFromNow_year > 0) {
                                    // 작년 혹은 그 이전 게시물
                                    friendNewImage.setBackgroundResource(R.drawable.drawable_empty)
                                } else {
                                    // 올해 게시물
                                    if (detailDateFromNow_month == 0) {
                                        // 같은 달
                                        if (detailDateFromNow_day == 0) {
                                            friendNewImage.setBackgroundResource(R.drawable.drawable_new_friend)
                                        } else if (detailDateFromNow_day > 3) {
                                            friendNewImage.setBackgroundResource(R.drawable.drawable_empty)
                                        } else {
                                            friendNewImage.setBackgroundResource(R.drawable.drawable_empty)
                                        }
                                    } else {
                                        friendNewImage.setBackgroundResource(R.drawable.drawable_empty)
                                    }
                                }
                            }
                            notifyDataSetChanged()
                        }
                    flag += 1
                }


                // 검색된 계정의 photo 필드를 바탕으로 ImageView에 Glide로 이미지 뷰 띄워줌
                val storageReference = storage!!.reference
                storageReference.child("user_profile/" + item.photo).downloadUrl.addOnSuccessListener {
                    Glide.with(friendLayout /* context */)
                        .load(it)
                        .into(friendImage)
                }
                friendName.setText(item.name)

                // 이미지 배경 동그랗게 !
                friendImage.setBackground(ShapeDrawable(OvalShape()))
                friendImage.setClipToOutline(true)

                // 한 계정 정보를 클릭 했을 때!! 친구의 마이페이지로 이동해야 한다!
                friendLayout.setOnClickListener{
                    // 친구의 마이 페이지로 가야함!
                    val intentFriendPage = Intent(view.context, Page_friendpage::class.java).apply{
                        val data = item.email
                        val flag = "friend"
                        putExtra("friendEmail", data)
                        putExtra("flag", flag)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    startActivity(intentFriendPage)
                }
            }
        }
    }
}
package kr.ac.cau.easyconnect

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

// 미완 ㅠㅠ 인스타그램처럼 친구추가 대신 팔로우 기능 넣으면 편할듯 합니다!

class Page_find_friends : AppCompatActivity() {

    var firebaseAuth: FirebaseAuth? = null
    var db: FirebaseFirestore? = null
    var storage: FirebaseStorage? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page_find_friends)

        // 권한과 파이어스토어 데이터베이스 객체 받아옴
        storage = FirebaseStorage.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // 검색 기본 옵션 = 이름!!
        var searchOption = "name"

        val recycler_view: RecyclerView = findViewById(R.id.recycler_friend)
        val button_goback : ImageButton = findViewById(R.id.bt_goback)
        val spinner_Item: Spinner = findViewById(R.id.spinner)
        val button_searchFriend: Button = findViewById(R.id.bt_searchFriend)
        val editText_searchFriend: EditText = findViewById(R.id.edit_searchFriend)

        recycler_view.adapter = FriendAdapter()
        recycler_view.layoutManager = LinearLayoutManager(this)

        // 선택 옵션 고를 수 있게! (이름 or 전화번호)
        spinner_Item.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

            // 이름 선택?! 전화번호 선택?!
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (spinner_Item.getItemAtPosition(position)) {
                    "이름" -> {
                        searchOption = "name"
                    }
                    "전화번호" -> {
                        searchOption = "phoneNumber"
                    }
                }
            }
        }

        button_goback.setOnClickListener({
            val intentMenu = Intent(this, Page_menu::class.java)
            startActivity(intentMenu)
            finish()
        })

        button_searchFriend.setOnClickListener({
            (recycler_view.adapter as FriendAdapter).search(
                editText_searchFriend.text.toString(),
                searchOption
            )
        })
    }

    inner class FriendAdapter() : RecyclerView.Adapter<FriendAdapter.FriendViewHolder>() {
        // 띄워줄 정보에 해당하는 UserDTO 객체들을 담을 리스트
        var arrayUserDTO: ArrayList<UserDTO> = arrayListOf()

        init {
            // 자신과 친구인 사람만 띄워야 함!! << 중요할 듯?

            // 초기화! 처음엔 안 띄움 / 원한다면 모든 회원 정보 띄울 수 있음!! for문 주석만 없애면 된다!!
            db!!.collection("user_information")
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    // userDTO 리스트 초기화
                    arrayUserDTO!!.clear()

                    for (snapshot in querySnapshot!!.documents) {
                        var user = snapshot.toObject(UserDTO::class.java)
                        if(user!!.email != firebaseAuth!!.currentUser.email){
                            // 자신의 정보는 출력할 필요가 없으므로 추가하지 않음
                            if(user!!.search == false){
                                // 검색 불허
                            }else{
                                arrayUserDTO!!.add(user!!)
                            }
                        }
                    }
                    notifyDataSetChanged()
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
            var friendFollow : TextView = view.findViewById(R.id.txt_follow)

            var userDTO = UserDTO()

            fun bind(item: UserDTO) {
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

                // 이미 친구?
                db!!.collection("user_information").whereEqualTo("email", firebaseAuth!!.currentUser.email).get().addOnCompleteListener {
                    if(it.isSuccessful){
                        for (dc in it.result!!.documents) {
                            userDTO = dc.toObject(UserDTO::class.java)!!
                            break
                        }
                        var myFollowing = userDTO.following!!.split(",").toMutableList() as ArrayList

                        friendFollow.setText("팬이 될래요!")
                        friendFollow.setBackgroundColor(Color.parseColor("#FFB8DFF8"))

                        for (email in myFollowing){
                            if(email == item.email){
                                friendFollow.setText("이미 팬이에요!")
                                friendFollow.setBackgroundColor(Color.parseColor("#FFD3D3D3"))
                            }
                        }
                    }
                }

                friendFollow.setOnClickListener({
                    var me = UserDTO()

                    if(friendFollow.text.toString() == "팬이 될래요!"){
                        friendFollow.setText("이미 팬이에요!");
                        friendFollow.setBackgroundColor(Color.parseColor("#FFD3D3D3"))

                        db!!.collection("user_information").whereEqualTo("email", firebaseAuth!!.currentUser.email).get().addOnCompleteListener {
                            if(it.isSuccessful){
                                for (dc in it.result!!.documents) {
                                    me = dc.toObject(UserDTO::class.java)!!
                                    break
                                }
                                // 팔로잉 했으니 나의 following 에 친구 이메일 추가
                                if(me.following.isNullOrEmpty()){
                                    // 나의 팔로잉 수 0명
                                    me.following = item.email
                                }else{
                                    // 나의 팔로잉 수 >= 1명
                                    me.following = me.following!! + ',' + item.email
                                }

                                // 팔로잉 했으니 친구의 followed 에 내 이메일 추가!
                                if(item.followed.isNullOrEmpty()) {
                                    // 친구의 팔로워 수 0명
                                    item.followed = me.email
                                }else{
                                    // 친구의 팔로워 수 >= 1명
                                    item.followed = item.followed + ',' + me.email
                                }
                                db!!.collection("user_information").document(me.uid.toString()).delete()
                                db!!.collection("user_information").document(me.uid.toString()).set(me)
                                db!!.collection("user_information").document(item.uid.toString()).delete()
                                db!!.collection("user_information").document(item.uid.toString()).set(item)
                            }
                        }
                    }else{
                        // 팔로잉 취소
                        db!!.collection("user_information").whereEqualTo("email", firebaseAuth!!.currentUser.email).get().addOnCompleteListener {
                            if(it.isSuccessful){
                                for (dc in it.result!!.documents) {
                                    me = dc.toObject(UserDTO::class.java)!!
                                    break
                                }
                                val my_following = me.following!!.split(",").toMutableList() as java.util.ArrayList
                                for (i in 0..my_following.size - 1){
                                    if(my_following[i] == item.email){
                                        my_following.removeAt(i)
                                        break
                                    }
                                }
                                val my_following_fin = my_following.joinToString(separator = ",")
                                me.following = my_following_fin

                                val friend_followed = item.followed!!.split(",").toMutableList() as java.util.ArrayList
                                for (j in 0..friend_followed.size - 1){
                                    if(friend_followed[j] == me.email){
                                        friend_followed.removeAt(j)
                                        break
                                    }
                                }
                                val friend_followed_fin = friend_followed.joinToString(separator = ",")
                                item.followed = friend_followed_fin

                                db!!.collection("user_information").document(me.uid.toString()).delete()
                                db!!.collection("user_information").document(me.uid.toString()).set(me)
                                db!!.collection("user_information").document(item.uid.toString()).delete()
                                db!!.collection("user_information").document(item.uid.toString()).set(item)
                            }
                        }

                        friendFollow.setText("팬이 될래요!")
                        friendFollow.setBackgroundColor(Color.parseColor("#FFB8DFF8"))
                    }
                })

                // 한 계정 정보를 클릭 했을 때!! 친구의 마이페이지로 이동해야 한다!

                friendLayout.setOnClickListener{
                    // 친구의 마이 페이지로 가야함!
                    val intentFriendPage = Intent(view.context, Page_friendpage::class.java).apply{
                        val data = item.email
                        val flag = "find"
                        putExtra("friendEmail", data)
                        putExtra("flag", flag)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    startActivity(intentFriendPage)
                }
            }
        }

        // 검색 함수
        fun search(serachWord: String, option: String) {
            db = FirebaseFirestore.getInstance()
            db!!.collection("user_information")
                ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    // ArrayList 비워줌
                    arrayUserDTO!!.clear()

                    for (snapshot in querySnapshot!!.documents) {
                        // searchWord로 들어온 정보가 포함된 계정을 리스트에 담는 과정!
                        if (snapshot.getString(option)!!.contains(serachWord)) {
                            var item = snapshot.toObject(UserDTO::class.java)
                            if(item!!.email != firebaseAuth!!.currentUser.email){
                                // 자신의 정보는 출력할 필요가 없으므로 추가하지 않음
                                if(item!!.search == false){
                                    // 검색 불허
                                }else{
                                    arrayUserDTO!!.add(item!!)
                                }
                            }
                        }
                    }
                    notifyDataSetChanged()
                }
        }

    }

    override fun onBackPressed(){
        // 클릭 시 이전 페이지인 메뉴로!
        val intentMenu = Intent(this, Page_menu::class.java)
        startActivity(intentMenu)
        finish()
    }
}
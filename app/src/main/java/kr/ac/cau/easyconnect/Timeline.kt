package kr.ac.cau.easyconnect

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.time.LocalDateTime

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

class Timeline : Fragment() {
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view : View = inflater.inflate(R.layout.fragment_timeline, container, false)
        val timelineView : RecyclerView = view.findViewById(R.id.recycer_photocardView)
        val context: Context = view.context
        val lm = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        timelineView.layoutManager = lm
        timelineView.adapter = PhotoCardAdapter()

        return view
    }

    inner class PhotoCardAdapter() : RecyclerView.Adapter<PhotoCardAdapter.PhotoCardViewHolder>(){
        // 로그인 한 유저의 작성 글 정보를 가져와서 배열에 담아두기 위한 것
        var arrayPostDTO : ArrayList<PostDTO> = arrayListOf()

        init{
            // 파이어스토어에서 작성 글에 관한 데이터베이스 정보를 가져온다.
            // 전부 띄워줘야 하므로 받아와서 arrayPostDTO 에 저장할 것
            db!!.collection("post").addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                arrayPostDTO.clear()

                var flag = 0
                // post_information에 있는 데이터 정보를 가져와서 각 정보마다 현재 로그인 한 유저의 글인지 비교해야함
                for (snapshot in querySnapshot!!.documents.reversed()) {
                    // 거꾸로 불러와야 가장 최신글이 제일 위에 뜰 수 있다! 게시일 기준으로 데이터베이스에 쌓이므로
                    var post = snapshot.toObject(PostDTO::class.java)

                    // 이름으로 비교해야 할 듯? 아니면 현재 유저의 이메일 정보로 이름 찾아와서 비교하던지..
                    if (post!!.name == firebaseAuth!!.currentUser.email) {
                        // 자신의 정보와 일치하는 글만 가져온다!
                        var now : String = LocalDateTime.now().toString()
                        var lastDate : String? = post.modified

                        var parsing_now = now.split("T")
                        var parsing_now_date = parsing_now[0].split("-")

                        var parsing_lastDate = lastDate!!.split("T")
                        var parsing_lastDate_date = parsing_lastDate[0].split("-")

                        var detailDateFromNow_year : Int = parsing_now_date[0].toInt() - parsing_lastDate_date[0].toInt()
                        var detailDateFromNow_month : Int = parsing_now_date[1].toInt() - parsing_lastDate_date[1].toInt()
                        var detailDateFromNow_day : Int = parsing_now_date[2].toInt() - parsing_lastDate_date[2].toInt()

                        var myDTO = UserDTO()

                        db!!.collection("user_information").whereEqualTo("email", firebaseAuth!!.currentUser.email).get().addOnCompleteListener {
                            if (it.isSuccessful) {
                                for (dc in it.result!!.documents) {
                                    myDTO = dc.toObject(UserDTO::class.java)!!
                                    break
                                }
                                if (detailDateFromNow_year  == 0){
                                    // 올해 게시물
                                    if (detailDateFromNow_month == 0) {
                                        // 같은 달
                                        if (detailDateFromNow_day == 0) {
                                            flag = 1

                                            myDTO.newPost = 1
                                            db!!.collection("user_information").document(myDTO.uid!!).delete()
                                            db!!.collection("user_information").document(myDTO.uid!!).set(myDTO)
                                        } else {
                                            if(flag == 0){
                                                myDTO.newPost = 0
                                                db!!.collection("user_information").document(myDTO.uid!!).delete()
                                                db!!.collection("user_information").document(myDTO.uid!!).set(myDTO)
                                            }
                                        }
                                    } else{
                                        if(flag == 0){
                                            myDTO.newPost = 0
                                            db!!.collection("user_information").document(myDTO.uid!!).delete()
                                            db!!.collection("user_information").document(myDTO.uid!!).set(myDTO)
                                        }
                                    }
                                }else{
                                    if(flag == 0){
                                        myDTO.newPost = 0
                                        db!!.collection("user_information").document(myDTO.uid!!).delete()
                                        db!!.collection("user_information").document(myDTO.uid!!).set(myDTO)
                                    }
                                }
                            }
                        }

                        arrayPostDTO!!.add(post!!)
                    }
                }
                notifyDataSetChanged()
            }
        }

        override fun getItemCount(): Int{
            return arrayPostDTO.size
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : PhotoCardViewHolder{
            val inflatedView =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.photocard_item_list, parent, false)
            return PhotoCardViewHolder(inflatedView)
        }

        @RequiresApi(Build.VERSION_CODES.O)
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
            var dateOfDetail: TextView = view.findViewById(R.id.date_of_post)
            var alarmNew : ImageView = view.findViewById(R.id.new_alarm)

            // 몇 일 전 게시글인지 보여주는!
            @RequiresApi(Build.VERSION_CODES.O)
            fun bind(item: PostDTO){
                var now : String = LocalDateTime.now().toString()
                var lastDate : String? = item.modified

                var parsing_now = now.split("T")
                var parsing_now_date = parsing_now[0].split("-")

                var parsing_lastDate = lastDate!!.split("T")
                var parsing_lastDate_date = parsing_lastDate[0].split("-")

                var detailDateFromNow_year : Int = parsing_now_date[0].toInt() - parsing_lastDate_date[0].toInt()
                var detailDateFromNow_month : Int = parsing_now_date[1].toInt() - parsing_lastDate_date[1].toInt()
                var detailDateFromNow_day : Int = parsing_now_date[2].toInt() - parsing_lastDate_date[2].toInt()

                if(!item.imageOfDetail.isNullOrEmpty()){
                    val storageReference = storage!!.reference
                    storageReference.child("post/" + item.imageOfDetail).downloadUrl.addOnSuccessListener {
                        Glide.with(photoCardLayout /* context */)
                                .load(it)
                                .into(photoOfDetail)
                    }
                }
                // 날짜 수정
                var myDTO = UserDTO()
                db!!.collection("user_information").whereEqualTo("email", firebaseAuth!!.currentUser.email).get().addOnCompleteListener {
                    if (it.isSuccessful) {
                        for (dc in it.result!!.documents) {
                            myDTO = dc.toObject(UserDTO::class.java)!!
                            break
                        }

                        if(detailDateFromNow_year > 0){
                            // 작년 혹은 그 이전 게시물
                            dateOfDetail.setText(parsing_lastDate[0])
                        }else{
                            // 올해 게시물
                            if(detailDateFromNow_month == 0){
                                // 같은 달
                                if(detailDateFromNow_day == 0){
                                    alarmNew.setBackgroundResource(R.drawable.drawable_new_post)
                                    dateOfDetail.setText("오늘")
                                }else if(detailDateFromNow_day > 3){
                                    alarmNew.setBackgroundResource(R.drawable.drawable_empty)
                                    dateOfDetail.setText(parsing_lastDate[0])
                                }else{
                                    alarmNew.setBackgroundResource(R.drawable.drawable_empty)
                                    dateOfDetail.setText(detailDateFromNow_day.toString() + "일 전")
                                }
                            }else{
                                alarmNew.setBackgroundResource(R.drawable.drawable_empty)
                                dateOfDetail.setText(parsing_lastDate[0])
                            }
                        }
                    }
                }



                nameOfDetail.setText(item.content)

                photoCardLayout.setOnClickListener{
                    val intentDetail = Intent(view.context, DetailMainActivity::class.java).apply{
                        val data = item.name + " " + item.modified
                        val flag = "mine"
                        putExtra("data", data)
                        putExtra("flag", flag)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    startActivity(intentDetail)
                }
            }

        }
    }
}
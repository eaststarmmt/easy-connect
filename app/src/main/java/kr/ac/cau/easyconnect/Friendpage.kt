package kr.ac.cau.easyconnect

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import org.w3c.dom.Text
import java.io.File
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

class Friendpage : Fragment() {
    var firebaseAuth : FirebaseAuth? = null
    var storage : FirebaseStorage? = null
    var db : FirebaseFirestore? = null

    var imgFileName : String? = null

    lateinit var imageView_me : ImageView
    var userDTO : UserDTO? = null

    private var friendEmail : String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let{
            friendEmail = it.getString("email")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view : View = inflater.inflate(R.layout.fragment_friendpage, container, false)
        val context: Context = view.context
        var friendCounter : Int

        imageView_me = view.findViewById(R.id.img_friend_profile)
        var txt_friend_name : TextView = view.findViewById(R.id.txt_friend_name_profile)
        var txt_friend_phonenumber : TextView = view.findViewById(R.id.txt_friend_phoneNumber_profile)
        var txt_following : TextView = view.findViewById(R.id.txt_following)
        var txt_followCount : TextView = view.findViewById(R.id.txt_friend_followed)

        // 권한과 파이어스토어 데이터베이스 객체 받아옴
        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // 현재 정보를 기본적으로 출력하는 부분!
        db!!.collection("user_information").whereEqualTo("email", friendEmail).get().addOnCompleteListener {
            if (it.isSuccessful) {
                // 친구 정보로 데이터 불러오기
                storage = FirebaseStorage.getInstance()
                val storageReference = storage!!.reference

                for (dc in it.result!!.documents) {
                    userDTO = dc.toObject(UserDTO::class.java)
                    break
                }
                if (userDTO != null) {
                    // 현재 로그인한 정보가 있다면 해당 정보들을 변수에 저장
                    imgFileName = userDTO!!.photo

                    // Url을 참조하여 해당 경로의 이미지를 읽어와서 Glide를 사용해 이미지뷰에 띄워주는 역할
                    storageReference.child("user_profile/" + imgFileName).downloadUrl.addOnSuccessListener {
                        Glide.with(this /* context */)
                            .load(it)
                            .into(imageView_me)
                    }

                    imageView_me.setBackground(ShapeDrawable(OvalShape()))
                    imageView_me.setClipToOutline(true)

                    txt_friend_name.setText(userDTO!!.name)
                    txt_friend_phonenumber.setText(userDTO!!.phoneNumber)
                    var myDTO = UserDTO()
                    var isHereFlag = false

                    db!!.collection("user_information").whereEqualTo("email", firebaseAuth!!.currentUser.email).get().addOnCompleteListener {
                        if(it.isSuccessful) {
                            for (dc in it.result!!.documents) {
                                myDTO = dc.toObject(UserDTO::class.java)!!
                                break
                            }
                        }
                        if(userDTO!!.followed.isNullOrEmpty()){
                            userDTO!!.followed = ""
                        }

                        val friend_followed = userDTO!!.followed!!.split(",").toMutableList() as ArrayList
                        friendCounter = friend_followed.size

                        if(friendCounter == 1 && friend_followed[0].isNullOrEmpty()){
                            txt_followCount.setText("친구의 팬 : " + 0)
                        }else{
                            txt_followCount.setText("친구의 팬 : " + friendCounter)
                        }
                        for (j in 0..friendCounter - 1){
                            if(friend_followed[j] == myDTO.email){
                                isHereFlag = true
                                break
                            }
                        }
                        if(isHereFlag){
                            txt_following.setText("이미 팬이에요!");
                            txt_following.setBackgroundColor(Color.parseColor("#FFD3D3D3"))
                        }else{
                            txt_following.setText("팬이 될래요!")
                            txt_following.setBackgroundColor(Color.parseColor("#FFB8DFF8"))
                        }
                    }
                }
            }
        }

        txt_following.setOnClickListener({
            var me = UserDTO()

            if(txt_following.text.toString() == "팬이 될래요!"){
                txt_following.setText("이미 팬이에요!");
                txt_following.setBackgroundColor(Color.parseColor("#FFD3D3D3"))

                db!!.collection("user_information").whereEqualTo("email", firebaseAuth!!.currentUser.email).get().addOnCompleteListener {
                    if(it.isSuccessful){
                        for (dc in it.result!!.documents) {
                            me = dc.toObject(UserDTO::class.java)!!
                            break
                        }
                        // 팔로잉 했으니 나의 following 에 친구 이메일 추가
                        if(me.following.isNullOrEmpty()){
                            // 나의 팔로잉 수 0명
                            me.following = userDTO!!.email
                        }else{
                            // 나의 팔로잉 수 >= 1명
                            me.following = me.following!! + ',' + userDTO!!.email
                        }

                        // 팔로잉 했으니 친구의 followed 에 내 이메일 추가!
                        if(userDTO!!.followed.isNullOrEmpty()) {
                            // 친구의 팔로워 수 0명
                            userDTO!!.followed = me.email
                        }else{
                            // 친구의 팔로워 수 >= 1명
                            userDTO!!.followed = userDTO!!.followed + ',' + me.email
                        }
                        val friend_followed = userDTO!!.followed!!.split(",").toMutableList() as ArrayList
                        friendCounter = friend_followed.size
                        txt_followCount.setText("친구의 팬 : " + friendCounter)
                        db!!.collection("user_information").document(me.uid.toString()).delete()
                        db!!.collection("user_information").document(me.uid.toString()).set(me)
                        db!!.collection("user_information").document(userDTO!!.uid.toString()).delete()
                        db!!.collection("user_information").document(userDTO!!.uid.toString()).set(userDTO!!)
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
                        val my_following = me.following!!.split(",").toMutableList() as ArrayList
                        for (i in 0..my_following.size - 1){
                            if(my_following[i] == userDTO!!.email){
                                my_following.removeAt(i)
                                break
                            }
                        }
                        val my_following_fin = my_following.joinToString(separator = ",")
                        me.following = my_following_fin

                        val friend_followed = userDTO!!.followed!!.split(",").toMutableList() as ArrayList
                        for (j in 0..friend_followed.size - 1){
                            if(friend_followed[j] == me.email){
                                friend_followed.removeAt(j)
                                break
                            }
                        }
                        friendCounter = friend_followed.size
                        txt_followCount.setText("친구의 팬 : " + friendCounter)
                        val friend_followed_fin = friend_followed.joinToString(separator = ",")
                        userDTO!!.followed = friend_followed_fin

                        db!!.collection("user_information").document(me.uid.toString()).delete()
                        db!!.collection("user_information").document(me.uid.toString()).set(me)
                        db!!.collection("user_information").document(userDTO!!.uid.toString()).delete()
                        db!!.collection("user_information").document(userDTO!!.uid.toString()).set(userDTO!!)
                    }
                }

                txt_following.setText("팬이 될래요!")
                txt_following.setBackgroundColor(Color.parseColor("#FFB8DFF8"))
            }
        })

        imageView_me.setOnClickListener({
            var builder = AlertDialog.Builder(it.context)
            var imageMeView : View? = layoutInflater.inflate(R.layout.image_expansion, null)
            var imageView_imageMe : ImageView? = imageMeView!!.findViewById(R.id.img_me_expanded)

            val storageReference = storage!!.reference
            storageReference.child("user_profile/" + userDTO!!.photo).downloadUrl.addOnSuccessListener {
                Glide.with(this /* context */)
                    .load(it)
                    .into(imageView_imageMe!!)
            }

            builder.setView(imageMeView)

            builder.setPositiveButton("확인", null)
            builder.show()
        })

        return view
    }
}
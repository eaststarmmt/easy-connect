package kr.ac.cau.easyconnect

import android.content.Context
import android.content.Intent
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
        imageView_me = view.findViewById(R.id.img_friend_profile)
        var txt_friend_name : TextView = view.findViewById(R.id.txt_friend_name_profile)
        var txt_friend_phonenumber : TextView = view.findViewById(R.id.txt_friend_phoneNumber_profile)

        // 권한과 파이어스토어 데이터베이스 객체 받아옴
        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // 현재 정보를 기본적으로 출력하는 부분!
        db!!.collection("user_information").whereEqualTo("email", friendEmail).get().addOnCompleteListener {
            if (it.isSuccessful) {
                // 현재 로그인 정보의 파이어스토어 데이터 조회
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
                }
            }
        }

        imageView_me.setOnClickListener({

            val intentImageMe = Intent(it.context, Page_imageme::class.java).apply{
                val userPhoto = userDTO!!.photo
                putExtra("userPhoto", userPhoto)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intentImageMe)
        })

        return view
    }
}
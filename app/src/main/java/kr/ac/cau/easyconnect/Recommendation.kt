package kr.ac.cau.easyconnect

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*
import kotlin.collections.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

class Recommendation : Fragment() {
    // TODO: Rename and change types of parameters

    var storage: FirebaseStorage? = null
    var firebaseAuth: FirebaseAuth? = null
    var db: FirebaseFirestore? = null

    var myDTO : UserDTO? = null

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

        val view : View = inflater.inflate(R.layout.fragment_recommendation, container, false)
        val context: Context = view.context
//        val lm = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
//        timelineView.layoutManager = lm
//        timelineView.adapter = PhotoCardAdapter()

        var textView_age : TextView = view.findViewById(R.id.txt_age)
        var button_age1 : Button = view.findViewById(R.id.btn_age1)
        var button_age2 : Button = view.findViewById(R.id.btn_age2)
        var button_age3 : Button = view.findViewById(R.id.btn_age3)
        var button_age4 : Button = view.findViewById(R.id.btn_age4)
        var button_age5 : Button = view.findViewById(R.id.btn_age5)
        var button_age6 : Button = view.findViewById(R.id.btn_age6)
        var button_gender1 : Button = view.findViewById(R.id.btn_gender1)
        var button_gender2 : Button = view.findViewById(R.id.btn_gender2)
        var button_gender3 : Button = view.findViewById(R.id.btn_gender3)
        var image_influencer1 : ImageView = view.findViewById(R.id.img_influencer1)
        var image_influencer2 : ImageView = view.findViewById(R.id.img_influencer2)
        var image_influencer3 : ImageView = view.findViewById(R.id.img_influencer3)
        var text_influencer1 : TextView = view.findViewById(R.id.txt_influencer1)
        var text_influencer2 : TextView = view.findViewById(R.id.txt_influencer2)
        var text_influencer3 : TextView = view.findViewById(R.id.txt_influencer3)

        var arrayAgeHashDTO : ArrayList<HashDTO> = arrayListOf()
        var arrayGenderHashDTO : ArrayList<HashDTO> = arrayListOf()
        var newArrayGenderHashDTO : ArrayList<HashDTO> = arrayListOf()

        // 현재 정보를 기본적으로 출력하는 부분!
        db!!.collection("user_information").whereEqualTo("email", firebaseAuth!!.currentUser.email).get().addOnCompleteListener {
            if (it.isSuccessful) {
                // 친구 정보로 데이터 불러오기
                var currentAge: String? = null

                for (dc in it.result!!.documents) {
                    myDTO = dc.toObject(UserDTO::class.java)
                    break
                }

                val age = myDTO!!.age!!.toInt()
                var ageStr : String? = null
                if(age < 20){
                    // 20세 미만
                    currentAge = "upto20"
                    ageStr = "20대 미만"
                }else if(age < 30){
                    // 20대
                    currentAge = "age20s"
                    ageStr = "20대"
                }else if(age < 40){
                    // 30대
                    currentAge = "age30s"
                    ageStr = "30대"
                }else if(age < 50){
                    // 40대
                    currentAge = "age40s"
                    ageStr = "40대"
                }else{
                    // 50대 이상
                    currentAge = "over50"
                    ageStr = "50대 이상"
                }
//                textView_age.setText("★ " + ageStr + "의 관심사는? ★")

                val gender = myDTO!!.gender

                db!!.collection("hashtag/" + currentAge + "/name").addSnapshotListener{ querySnapshot, firebaseFirestoreException ->
                    arrayAgeHashDTO.clear()
                    for(snapshot in querySnapshot!!.documents!!){
                        var ageHash = snapshot.toObject(HashDTO::class.java)

                        arrayAgeHashDTO.add(ageHash!!)
                    }
                    arrayAgeHashDTO.sortByDescending{it.count}

                    button_age1.setText(arrayAgeHashDTO.get(0).name.toString())
                    button_age2.setText(arrayAgeHashDTO.get(1).name.toString())
                    button_age3.setText(arrayAgeHashDTO.get(2).name.toString())
                    button_age4.setText(arrayAgeHashDTO.get(3).name.toString())
                    button_age5.setText(arrayAgeHashDTO.get(4).name.toString())
                    button_age6.setText(arrayAgeHashDTO.get(5).name.toString())

                    db!!.collection("hashtag/" + gender + "/name").addSnapshotListener{ querySnapshot, firebaseFirestoreException ->
                        arrayGenderHashDTO.clear()
                        for(snapshot in querySnapshot!!.documents!!){
                            var genderHash = snapshot.toObject(HashDTO::class.java)

                            arrayGenderHashDTO.add(genderHash!!)
                        }
                        arrayGenderHashDTO.sortByDescending{it.count}

                        for(genderHashDTO in arrayGenderHashDTO){
                            if(genderHashDTO.name.toString() != arrayAgeHashDTO.get(0).name.toString() &&
                                genderHashDTO.name.toString() != arrayAgeHashDTO.get(1).name.toString() &&
                                        genderHashDTO.name.toString() != arrayAgeHashDTO.get(2).name.toString() &&
                                                genderHashDTO.name.toString() != arrayAgeHashDTO.get(3).name.toString() &&
                                                        genderHashDTO.name.toString() != arrayAgeHashDTO.get(4).name.toString() &&
                                                                genderHashDTO.name.toString() != arrayAgeHashDTO.get(5).name.toString()) {
                                newArrayGenderHashDTO.add(genderHashDTO)
                            }
                        }

                        button_gender1.setText(newArrayGenderHashDTO.get(0).name.toString())
                        button_gender2.setText(newArrayGenderHashDTO.get(1).name.toString())
                        button_gender3.setText(newArrayGenderHashDTO.get(2).name.toString())
                    }
                }
            }
        }

        storage = FirebaseStorage.getInstance()
        val storageReference = storage!!.reference
        var arrayUserDTO: ArrayList<UserDTO> = arrayListOf()

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
                arrayUserDTO.sortByDescending{it.followerCount}

                var arrayRecommendUserDTO: ArrayList<UserDTO> = arrayListOf()
                for(index in 0..4){
                    arrayRecommendUserDTO.add(arrayUserDTO.get(index))
                }

                Collections.shuffle(arrayRecommendUserDTO)

                // Url을 참조하여 해당 경로의 이미지를 읽어와서 Glide를 사용해 이미지뷰에 띄워주는 역할
                storageReference.child("user_profile/" + arrayRecommendUserDTO.get(0).photo).downloadUrl.addOnSuccessListener {
                    Glide.with(this /* context */)
                        .load(it)
                        .into(image_influencer1)
                }
                text_influencer1.setText(arrayRecommendUserDTO.get(0).name)

                // Url을 참조하여 해당 경로의 이미지를 읽어와서 Glide를 사용해 이미지뷰에 띄워주는 역할
                storageReference.child("user_profile/" + arrayRecommendUserDTO.get(1).photo).downloadUrl.addOnSuccessListener {
                    Glide.with(this /* context */)
                        .load(it)
                        .into(image_influencer2)
                }
                text_influencer2.setText(arrayRecommendUserDTO.get(1).name)

                // Url을 참조하여 해당 경로의 이미지를 읽어와서 Glide를 사용해 이미지뷰에 띄워주는 역할
                storageReference.child("user_profile/" + arrayRecommendUserDTO.get(2).photo).downloadUrl.addOnSuccessListener {
                    Glide.with(this /* context */)
                        .load(it)
                        .into(image_influencer3)
                }
                text_influencer3.setText(arrayRecommendUserDTO.get(2).name)
            }

        button_age1.setOnClickListener({
            val intentHashtagPage = Intent(view.context, Page_hashtag::class.java).apply{
                val text = arrayAgeHashDTO.get(0).name.toString()
                putExtra("text", text)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intentHashtagPage)
        })

        button_age2.setOnClickListener({
            val intentHashtagPage = Intent(view.context, Page_hashtag::class.java).apply{
                val text = arrayAgeHashDTO.get(1).name.toString()
                putExtra("text", text)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intentHashtagPage)
        })

        button_age3.setOnClickListener({
            val intentHashtagPage = Intent(view.context, Page_hashtag::class.java).apply{
                val text = arrayAgeHashDTO.get(2).name.toString()
                putExtra("text", text)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intentHashtagPage)
        })

        button_age4.setOnClickListener({
            val intentHashtagPage = Intent(view.context, Page_hashtag::class.java).apply{
                val text = arrayAgeHashDTO.get(3).name.toString()
                putExtra("text", text)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intentHashtagPage)
        })

        button_age5.setOnClickListener({
            val intentHashtagPage = Intent(view.context, Page_hashtag::class.java).apply{
                val text = arrayAgeHashDTO.get(4).name.toString()
                putExtra("text", text)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intentHashtagPage)
        })

        button_age6.setOnClickListener({
            val intentHashtagPage = Intent(view.context, Page_hashtag::class.java).apply{
                val text = arrayAgeHashDTO.get(5).name.toString()
                putExtra("text", text)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intentHashtagPage)
        })

        button_gender1.setOnClickListener({
            val intentHashtagPage = Intent(view.context, Page_hashtag::class.java).apply{
                val text = newArrayGenderHashDTO.get(0).name.toString()
                putExtra("text", text)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intentHashtagPage)
        })

        button_gender2.setOnClickListener({
            val intentHashtagPage = Intent(view.context, Page_hashtag::class.java).apply{
                val text = newArrayGenderHashDTO.get(1).name.toString()
                putExtra("text", text)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intentHashtagPage)
        })

        button_gender3.setOnClickListener({
            val intentHashtagPage = Intent(view.context, Page_hashtag::class.java).apply{
                val text = newArrayGenderHashDTO.get(2).name.toString()
                putExtra("text", text)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intentHashtagPage)
        })

        return view
    }
}
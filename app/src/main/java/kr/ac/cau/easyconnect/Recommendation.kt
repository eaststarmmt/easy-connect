package kr.ac.cau.easyconnect

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

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

        var arrayAgeHashDTO : ArrayList<HashDTO> = arrayListOf()
        var arrayGenderHashDTO : ArrayList<HashDTO> = arrayListOf()
        var newArrayGenderHashDTO : ArrayList<HashDTO> = arrayListOf()

        // 권한과 파이어스토어 데이터베이스 객체 받아옴
        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

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
                textView_age.setText("★ " + ageStr + "의 관심사는? ★")

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

        return view
    }
}
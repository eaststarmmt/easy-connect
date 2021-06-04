package kr.ac.cau.easyconnect

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.ClipData
import android.content.DialogInterface
import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList

class WriteActivity : AppCompatActivity() {

    var firebaseAuth: FirebaseAuth? = null
    var storage : FirebaseStorage? = null

    val REQUEST_IMAGE_CAPTURE = 1
    val REQUEST_GALLERY_TAKE = 2
    val GET_GALLERY_IMAGE = 200

    var imgFileName : String? = null
    var imgNameList: Array<String?> = arrayOfNulls(3)
    var uriPhoto : Uri? = null
    var clipData: ClipData? = null

    var uriList: Array<Uri?> = arrayOfNulls(3)

    lateinit var currentPhotoPath : String
    lateinit var imageView : ImageView
    lateinit var imageView2 : ImageView
    lateinit var imageView3 : ImageView
    lateinit var content: MultiAutoCompleteTextView
    val timestamp: String = SimpleDateFormat("yyyyMMdd_HHmmsss").format(Date())

    private var speechRecognizer: SpeechRecognizer? = null
    private var REQUEST_CODE = 1

    lateinit var userData : UserDTO
    lateinit var age : String
    lateinit var gender : String

    lateinit var checkAge : String
    lateinit var checkGender : String

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write)
        if (Build.VERSION.SDK_INT >= 23)
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.INTERNET, Manifest.permission.RECORD_AUDIO), REQUEST_CODE)

        // Firebase - Auth, Firestore의 인스턴스 받아오기
        firebaseAuth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        // xml id 연결
//        val title: EditText = findViewById(R.id.title)
        content = findViewById(R.id.content)
        imageView = findViewById(R.id.imageView)
        imageView2 = findViewById(R.id.imageView2)
        imageView3 = findViewById(R.id.imageView3)

        //연령대 미리 받아두기
        db.collection("user_information").whereEqualTo("email", firebaseAuth!!.currentUser.email).get().addOnCompleteListener {
            for (dc in it.result!!.documents) {
                var data = dc.toObject(UserDTO::class.java)
                if (data != null) {
                    userData = data
                }
                break
            }

            age = userData!!.age.toString()
            gender = userData!!.gender.toString()
        }

        findViewById<ImageButton>(R.id.back).setOnClickListener {

            val dialog = AlertDialog.Builder(this)
            dialog.setTitle("작성된 내용이 저장되지 않습니다. \n종료하시겠습니까? ")
            // 확인시 종료 처리 할 리스너
            var listener = DialogInterface.OnClickListener { dialog, i ->
                finish()
            }
            dialog.setPositiveButton("확인", listener)
            dialog.setNegativeButton("취소", null)
            dialog.show()
        }
        // 취소버튼 눌렀을 때 구현
        findViewById<Button>(R.id.cancel).setOnClickListener {
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle("작성된 내용이 저장되지 않습니다. \n종료하시겠습니까? ")
            // 확인시 종료 처리 할 리스너
            var listener = DialogInterface.OnClickListener { dialog, i ->
                finish()
            }
            dialog.setPositiveButton("확인", listener)
            dialog.setNegativeButton("취소", null)
            dialog.show()
        }

        // 사진추가
        findViewById<Button>(R.id.photo).setOnClickListener {
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle("사진 추가할 방법 선택")
            // 촬영 누른 경우
            var cameraListener = DialogInterface.OnClickListener { dialog, i ->
                takePicture()
            }
            // 앨범 누른 경우
            var albumListener = DialogInterface.OnClickListener { dialog, i ->
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "image/*"
                startActivityForResult(intent, REQUEST_GALLERY_TAKE)
            }
            dialog.setPositiveButton("촬영", cameraListener)
            dialog.setNeutralButton("취소", null)
            dialog.setNegativeButton("앨범", albumListener)
            dialog.show()
        }

        // 게시버튼 눌렀을 때 구현

        findViewById<Button>(R.id.post).setOnClickListener {
    //        var inputTitle = title.text.trim().toString()
            var inputContent = content.text.trim().toString()
            var name = firebaseAuth!!.currentUser.email.toString()
            var registered : String = LocalDateTime.now().toString()
            var modified : String = LocalDateTime.now().toString()
            var imgOfDetail : String? = imgNameList[0]
            var imgOfDetail2 : String? = imgNameList[1]
            var imgOfDetail3 : String? = imgNameList[2]

            var hashtagList : MutableList<String> = mutableListOf() // 해시태그 받을 동적 배열 생성
            val splitArray = content.text.split(" ")    // 공백을 기준으로 입력된 문자열 전체를 자름
            for (a in splitArray) {     // 해시태그가 있는지 검사
                if ('#' in a) {
                    hashtagList.add(a.substring(a.indexOf("#")))    // #부터 문자열 공백 전까지 문자 받기기
               }
            }
            // 해시태그 등록
            // TODO: 태그 있는지 조회 후 중복이면 카운팅만 하도록 설계해야됨
            var htDTO : HashDTO? = null

            // 전체 total case
            for (ht in hashtagList) {
                db.collection("hashtag/total/name").whereEqualTo("name", ht).get().addOnCompleteListener{
                    htDTO = null
                    if(it.isSuccessful){
                        for (dc in it.result!!.documents) {
                            htDTO = dc.toObject(HashDTO::class.java)
                            break
                        }

                        var map = mutableMapOf<String, Any?>()
                        if(htDTO != null){
                            map["count"] = htDTO!!.count!!.toInt() + 1

                            db.collection("hashtag/total/name").document(
                                ht
                            ).update(map)
                        }
                        else{
                            var new_count = 1
                            val new_htDTO = HashDTO(ht, new_count)
                            db.collection("hashtag/total/name").document(ht).set(new_htDTO)
                        }
                    }
                }
            }

            var myDTO = UserDTO()
            db!!.collection("user_information").whereEqualTo("email", firebaseAuth!!.currentUser.email).get().addOnCompleteListener {
                if (it.isSuccessful) {
                    for (dc in it.result!!.documents) {
                        myDTO = dc.toObject(UserDTO::class.java)!!
                        break
                    }

                    val age = myDTO.age!!.toInt()
                    var current_age : String? = null
                    if(age < 20){
                        // 20세 미만
                        current_age = "upto20"
                    }else if(age < 30){
                        // 20대
                        current_age = "age20s"
                    }else if(age < 40){
                        // 30대
                        current_age = "age30s"
                    }else if(age < 50){
                        // 40대
                        current_age = "age40s"
                    }else{
                        // 50대 이상
                        current_age = "over50"
                    }

                    // 나이 case
                    for (ht in hashtagList) {
                        db.collection("hashtag/" + current_age + "/name").whereEqualTo("name", ht).get().addOnCompleteListener{ query ->
                            htDTO = null

                            if(query.isSuccessful){
                                for (dc in query.result!!.documents) {
                                    htDTO = dc.toObject(HashDTO::class.java)
                                    break
                                }

                                var map = mutableMapOf<String, Any?>()
                                if(htDTO != null){
                                    map["count"] = htDTO!!.count!!.toInt() + 1

                                    db.collection("hashtag/" + current_age + "/name").document(
                                        ht
                                    ).update(map)
                                }
                                else{
                                    var new_count = 1
                                    val new_htDTO = HashDTO(ht, new_count)
                                    db.collection("hashtag/" + current_age + "/name").document(ht).set(new_htDTO)
                                }
                            }
                        }
                    }
                }
            }

            var myDTO2 = UserDTO()
            db!!.collection("user_information").whereEqualTo("email", firebaseAuth!!.currentUser.email).get().addOnCompleteListener {
                if (it.isSuccessful) {
                    for (dc in it.result!!.documents) {
                        myDTO2 = dc.toObject(UserDTO::class.java)!!
                        break
                    }
                    val current_gender = myDTO2.gender

                    // 성별 case
                    for (ht in hashtagList) {
                        db.collection("hashtag/" + current_gender + "/name").whereEqualTo("name", ht).get().addOnCompleteListener{ query ->
                            htDTO = null
                            if(query.isSuccessful){
                                for (dc in query.result!!.documents) {
                                    htDTO = dc.toObject(HashDTO::class.java)
                                    break
                                }

                                var map = mutableMapOf<String, Any?>()
                                if(htDTO != null){
                                    map["count"] = htDTO!!.count!!.toInt() + 1

                                    db.collection("hashtag/" + current_gender + "/name").document(ht).update(map)
                                }
                                else{
                                    var new_count = 1
                                    val new_htDTO = HashDTO(ht, new_count)
                                    db.collection("hashtag/" + current_gender + "/name").document(ht).set(new_htDTO)
                                }
                            }
                        }
                    }
                }
            }
            val postDTO : PostDTO = PostDTO(null, inputContent, name, registered, modified,
                imgOfDetail, imgOfDetail2, imgOfDetail3)

            if (inputContent.isNullOrEmpty()) {
                var builder = AlertDialog.Builder(this)
                builder.setTitle("내용을 입력해주세요.")
                builder.setPositiveButton("확인", null)
                builder.show()
            } else {
                if (!imgOfDetail.isNullOrEmpty()){
                    imageUpload()
                    db.collection("post").document(registered).set(postDTO).addOnCompleteListener(this) {
                        //글이 정상적으로 작성 됐을 때
                        if (it.isSuccessful) {
                            //Toast.makeText(this, "success", Toast.LENGTH_SHORT).show()
                            //현재 엑티비티 종료하고 내가 쓴 글 확인하는 액티비티로 이동. 추후에 수정 예정
                            //val intent = Intent(this, DetailActivity::class.java)
                            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)   Detail이 끝나고 바로 메인으로 가기 위해 만듬
                            //startActivity(intent)
                            val loadingAnimDialog = CustomLodingDialog(this)
                            loadingAnimDialog.show()
                            Handler().postDelayed({
                                loadingAnimDialog.dismiss()
                                val intentMain = Intent(this, MainActivity::class.java)
                                startActivity(intentMain)
                                finish()
                            }, 15000)


                        } else {
                            Toast.makeText(this, "failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                }else{
                    db.collection("post").document(registered).set(postDTO).addOnCompleteListener(this){
                        if (it.isSuccessful) {
                            val intentMain = Intent(this, MainActivity::class.java)
                            startActivity(intentMain)
                            finish()
                        }else {
                            Toast.makeText(this, "failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        // 녹음버튼
        findViewById<Button>(R.id.record).setOnClickListener {
            startSTTUseActivityResult()
        }

        //연령별 리스트 불러오기
        var myDTO = UserDTO()
        db!!.collection("user_information").whereEqualTo("email", firebaseAuth!!.currentUser.email).get().addOnCompleteListener {
            if (it.isSuccessful) {
                for (dc in it.result!!.documents) {
                    myDTO = dc.toObject(UserDTO::class.java)!!
                    break
                }

                val age = myDTO.age!!.toInt()
                var current_age: String? = null
                if (age < 20) {
                    // 20세 미만
                    current_age = "upto20"
                } else if (age < 30) {
                    // 20대
                    current_age = "age20s"
                } else if (age < 40) {
                    // 30대
                    current_age = "age30s"
                } else if (age < 50) {
                    // 40대
                    current_age = "age40s"
                } else {
                    // 50대 이상
                    current_age = "over50"
                }
                db.collection("hashtag/" + current_age + "/name").get()
                    .addOnCompleteListener { query ->
                        var hashDTO: HashDTO? = null
                        var nameObject: MutableList<String> = mutableListOf()
                        var countObject: MutableList<Int> = mutableListOf()
                        var hashObject : MutableList<WriteActivity.Movie?> = mutableListOf()

                        if (query.isSuccessful) {
                            var i = 0
                            for (dc in query.result!!.documents) {
                                hashDTO = dc.toObject(HashDTO::class.java)
                                hashObject.add(Movie(hashDTO!!.name.toString(), hashDTO!!.count!!.toInt()))

                            }
                            hashObject.sortByDescending { it!!.year }
                            val adapter = AutoCompleteAdapter(this, R.layout.item_auto_complete_text_view, hashObject)
                            content.threshold = 2  // 두 글자부터 드롭다운
                            content.setAdapter(adapter)
                            content.setTokenizer(SpaceTokenizer())
                        }
                    }
            }
        }
//
//        content.threshold = 1  // 두 글자부터 드롭다운
//        content.setAdapter(adapter)    // 어댑터 설정
//        content.setTokenizer(SpaceTokenizer()) // 공백으로 구분하기 위해

    }


    // STT 구현
    private fun startSTTUseActivityResult() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        val speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }

        startActivityForResult(speechRecognizerIntent, 100)
    }



    // 뒤로가기 구현
    override fun onBackPressed(){
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("작성된 내용이 저장되지 않을수 있습니다.\n종료하시겠습니까? ")
        // 확인시 종료 처리 할 리스너
        var listener = DialogInterface.OnClickListener { dialog, i ->
            val intentMain = Intent(this, MainActivity::class.java)
            startActivity(intentMain)
            finish()
        }
        dialog.setPositiveButton("확인", listener)
        dialog.setNegativeButton("취소", null)
        dialog.show()
    }
    // 카메라 사용
    private fun takePicture() {
        storage = FirebaseStorage.getInstance()

        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            if (takePictureIntent.resolveActivity(this.packageManager) != null) {
                // 찍은 사진 이미지로 변환
                val photoFile: File? =
                        try {
                            createImageFile()
                        } catch (ex: IOException) {
                            Log.d("TAG", "그림파일 만드는도중 에러생김")
                            null
                        }
                // onActivityForResult로 전달
                photoFile?.also {
                    val photoUri: Uri = FileProvider.getUriForFile(
                            this, "kr.ac.cau.easyconnect.fileprovider", it
                    )

                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }
    // 카메라로 촬영한 이미지를 파일로 저장해준다
    @Throws(IOException::class)
    private  fun createImageFile(): File {
        // Create an image file name
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        imgFileName = "IMAGE_" + timestamp + "_.jpg"

        return File.createTempFile(
                "JPEG_${timestamp}_", /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }

    }

    override fun onDestroy() {
        if (speechRecognizer != null) {
            speechRecognizer!!.stopListening()
        }

        super.onDestroy()
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == 100) {
            val st: String = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)!![0]
            content.setText(st)
        //    content.text = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)!![0]
        }
        when(requestCode) {
            // 카메라 쵤영
            REQUEST_IMAGE_CAPTURE -> {
                if (resultCode == Activity.RESULT_OK) {
                    // 카메라로부터 받은 데이터 있을때
                    val file = File(currentPhotoPath)
                    val decode = ImageDecoder.createSource(
                            this.contentResolver,
                            Uri.fromFile(file)
                    )
                    val bitmap = ImageDecoder.decodeBitmap(decode)
                    imageView.setImageBitmap(bitmap)
                    uriList[0] = Uri.fromFile(file)
                    imgNameList[0] = "IMAGE_" + timestamp + "_.jpg"
                }
            }
            // 앨범에서 가져왔을 때
/*
            REQUEST_GALLERY_TAKE -> {
                // Uri
                if (resultCode == Activity.RESULT_OK) {
                    uriPhoto = data?.data
                    imageView.setImageURI(uriPhoto) // handle chosen image
                    imgFileName = "IMAGE_" + timestamp + "_.jpg"
                }
            }

*/
            REQUEST_GALLERY_TAKE -> {
                if(data == null) {
                    Toast.makeText(applicationContext,"이미지를 선택하지 않았습니다.", Toast.LENGTH_LONG).show()
                } else {
                    clipData = data.clipData
                    if(data.clipData == null) {
                        uriList[0] = data.data
                        imgNameList[0] = "IMAGE_" + timestamp + "_.jpg"
                        imageView.setImageURI(uriList[0])
                    } else {
                        for (i in 0 until clipData!!.itemCount) {
                            uriList[i] = clipData!!.getItemAt(i).uri
                            if (i == 0) {
                                imgNameList[i] = "IMAGE_" + timestamp + "_.jpg"
                                imageView.setImageURI(uriList[i])
                            } else if (i == 1) {
                                imgNameList[i] = "IMAGE_" + timestamp + "-" + (i+1) + "_.jpg"
                                imageView2.setImageURI(uriList[i])
                            } else if (i == 2) {
                                imgNameList[i] = "IMAGE_" + timestamp + "-" + (i+1) + "_.jpg"
                                imageView3.setImageURI(uriList[i])
                            }
                        }
                    }
                }
            }


        }


    }


    private fun imageUpload() {
        var postDTO : PostDTO? = null

        val progressDialog : ProgressDialog = ProgressDialog(this)

        // 결정되는대로 바꿀예정
        imgFileName = "IMAGE_" + timestamp + "_.jpg"

        storage = FirebaseStorage.getInstance()
        val db = FirebaseFirestore.getInstance()

        if (uriList[1] == null) {
            var riversRef = storage!!.reference.child("post").child(imgNameList[0]!!)
            riversRef.putFile(uriList[0]!!)
                .addOnSuccessListener {

                    riversRef.downloadUrl.addOnSuccessListener { uri ->
                        db.collection("user_information")
                            .whereEqualTo("email", firebaseAuth!!.currentUser.email).get()
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    for (dc in it.result!!.documents) {
                                        postDTO = dc.toObject(PostDTO::class.java)
                                        break
                                    }
                                }
                            }
                    }
                }

        }
        else {
            for (i in 0 until clipData!!.itemCount) {
                // url 찾기 위해 참조!
                var riversRef = storage!!.reference.child("post").child(imgNameList[i]!!)
                //riversRef.putFile(uriPhoto!!)
                riversRef.putFile(uriList[i]!!)
                    .addOnSuccessListener {

                        riversRef.downloadUrl.addOnSuccessListener { uri ->
                            db.collection("user_information")
                                .whereEqualTo("email", firebaseAuth!!.currentUser.email).get()
                                .addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        for (dc in it.result!!.documents) {
                                            postDTO = dc.toObject(PostDTO::class.java)
                                            break
                                        }
                                    }
                                }
                        }
                    }

            }
        }
    //    Thread.sleep(10000)

    }

    data class Movie(
        val name: String,
        val year: Int
    ) {
        override fun toString(): String {
            return name
        }
    }


}
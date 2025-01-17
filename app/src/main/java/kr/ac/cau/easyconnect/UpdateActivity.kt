package kr.ac.cau.easyconnect

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
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*

class UpdateActivity : AppCompatActivity() {

    val REQUEST_IMAGE_CAPTURE = 1
    val REQUEST_GALLERY_TAKE = 2

    val timestamp: String = SimpleDateFormat("yyyyMMdd_HHmmsss").format(Date())
    lateinit var currentPhotoPath : String
    var clipData: ClipData? = null

    var firebaseAuth: FirebaseAuth? = null
    var storage: FirebaseStorage? = null

    var imgFileName: String? = null
    var imgFileName2: String? = null
    var imgFileName3: String? = null
    var imgNameList: Array<String?> = arrayOfNulls(3)
    lateinit var imageView : ImageView
    lateinit var imageView2 : ImageView
    lateinit var imageView3 : ImageView
    lateinit var content: EditText
    var uriList: Array<Uri?> = arrayOfNulls(3)

    private var speechRecognizer: SpeechRecognizer? = null
    private var REQUEST_CODE = 1

    var map = mutableMapOf<String, Any?>()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update)



        // Firebase - Auth, Firestore의 인스턴스 받아오기
        firebaseAuth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
 //       var title: EditText = findViewById(R.id.editTitle)
        imageView = findViewById(R.id.imageView)
        imageView2 = findViewById(R.id.imageView2)
        imageView3 = findViewById(R.id.imageView3)
        content = findViewById(R.id.content)
        var postDTO: PostDTO? = null

        db.collection("post").whereEqualTo("name", firebaseAuth!!.currentUser.email).get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    // 파이어스토어에서 현재 게시글 정보 조회
                    storage = FirebaseStorage.getInstance()
                    val storageReference = storage!!.reference

                    for (dc in it.result!!.documents.reversed()) {
                        postDTO = dc.toObject(PostDTO::class.java)
                        break
                    }
                    // 게시글 정보 받아오기
                    if (postDTO != null) {
                        content.setText(postDTO!!.content)
                        imgFileName = postDTO!!.imageOfDetail
                        imgFileName2 = postDTO!!.imageOfDetail2
                        imgFileName3 = postDTO!!.imageOfDetail3

                        if (imgFileName != null) {

                            storageReference.child("post/" + imgFileName).downloadUrl.addOnSuccessListener {
                                Glide.with(this)
                                    .load(it)
                                    .into(imageView)
                            }

                        }
                        if (imgFileName2 != null) {
                            storageReference.child("post/" + imgFileName2).downloadUrl.addOnSuccessListener {
                                Glide.with(this)
                                    .load(it)
                                    .into(imageView2)
                            }
                        }
                        if (imgFileName3 != null) {
                            storageReference.child("post/" + imgFileName3).downloadUrl.addOnSuccessListener {
                                Glide.with(this)
                                    .load(it)
                                    .into(imageView3)
                            }
                        }

                    }
                }
            }
        // 앨범버튼 눌렀을 때
        findViewById<Button>(R.id.album).setOnClickListener {

            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_GALLERY_TAKE)
        }

        // 카메라 촬영 버튼 눌렀을 때
        findViewById<Button>(R.id.photoButton).setOnClickListener {
            takePicture()
        }


        // 수정버튼 클릭시
        findViewById<Button>(R.id.update).setOnClickListener {
//            db.collection("post").document(postDTO!!.modified.toString()).delete()
            postDTO!!.content = content.text.trim().toString()
//            postDTO!!.modified = LocalDateTime.now().toString()

            map["content"] = content.text.trim().toString()
            db.collection("post").document(postDTO!!.registered.toString()).update(map)
                .addOnCompleteListener {
                    if(it.isSuccessful) {
                       // imageUpload()
                        val loadingAnimDialog = CustomLodingDialog(this)
                        loadingAnimDialog.show()
                        Handler().postDelayed({
                            loadingAnimDialog.dismiss()
                            finish()
                        }, 13000)
                    }
                }
        }

        findViewById<Button>(R.id.cancel).setOnClickListener {
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle("작성된 내용이 저장되지 않을수 있습니다.\n종료하시겠습니까? ")
            // 확인시 종료 처리 할 리스너
            var listener = DialogInterface.OnClickListener { dialog, i ->
                finish()
            }
            dialog.setPositiveButton("확인", listener)
            dialog.setNegativeButton("취소", null)
            dialog.show()

        }

        findViewById<ImageButton>(R.id.back).setOnClickListener {
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle("작성된 내용이 저장되지 않을수 있습니다.\n종료하시겠습니까? ")
            // 확인시 종료 처리 할 리스너
            var listener = DialogInterface.OnClickListener { dialog, i ->
                finish()
            }
            dialog.setPositiveButton("확인", listener)
            dialog.setNegativeButton("취소", null)
            dialog.show()

        }
        //녹음버튼
        findViewById<Button>(R.id.record).setOnClickListener {
            startSTTUseActivityResult()
        }

        imageView.setOnLongClickListener {
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle("사진을 삭제 하시겠습니까? ")

            var listener = DialogInterface.OnClickListener { dialog, i ->
                storage = FirebaseStorage.getInstance()
                val storageReference = storage!!.reference
                storageReference.child("post/null.PNG").downloadUrl.addOnSuccessListener {
                    Glide.with(this)
                        .load(it)
                        .into(imageView)
                }
                map["imageOfDetail"] = null
            }
            dialog.setPositiveButton("확인", listener)
            dialog.setNegativeButton("취소", null)
            dialog.show()
            return@setOnLongClickListener true
        }

        imageView2.setOnLongClickListener {
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle("사진을 삭제 하시겠습니까? ")

            var listener = DialogInterface.OnClickListener { dialog, i ->
                storage = FirebaseStorage.getInstance()
                val storageReference = storage!!.reference
                storageReference.child("post/null.PNG").downloadUrl.addOnSuccessListener {
                    Glide.with(this)
                        .load(it)
                        .into(imageView2)
                }
                map["imageOfDetail2"] = null
            }
            dialog.setPositiveButton("확인", listener)
            dialog.setNegativeButton("취소", null)
            dialog.show()
            return@setOnLongClickListener true
        }

        imageView3.setOnLongClickListener {
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle("사진을 삭제 하시겠습니까? ")

            var listener = DialogInterface.OnClickListener { dialog, i ->
                storage = FirebaseStorage.getInstance()
                val storageReference = storage!!.reference
                storageReference.child("post/null.PNG").downloadUrl.addOnSuccessListener {
                    Glide.with(this)
                        .load(it)
                        .into(imageView3)
                }
                map["imageOfDetail3"] = null
            }
            dialog.setPositiveButton("확인", listener)
            dialog.setNegativeButton("취소", null)
            dialog.show()
            return@setOnLongClickListener true
        }


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

    override fun onBackPressed(){
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("작성된 내용이 저장되지 않을수 있습니다.\n종료하시겠습니까? ")
        // 확인시 종료 처리 할 리스너
        var listener = DialogInterface.OnClickListener { dialog, i ->
            finish()
        }
        dialog.setPositiveButton("확인", listener)
        dialog.setNegativeButton("취소", null)
        dialog.show()
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
                    map["imageOfDetail"] = imgFileName
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
                        map["imageOfDetail"] = imgFileName
                    } else {
                        for (i in 0 until clipData!!.itemCount) {
                            uriList[i] = clipData!!.getItemAt(i).uri
                            if (i == 0) {
                                imgNameList[i] = "IMAGE_" + timestamp + "_.jpg"
                                imageView.setImageURI(uriList[i])
                                map["imageOfDetail"] = imgNameList[i]
                            } else if (i == 1) {
                                imgNameList[i] = "IMAGE_" + timestamp + "-" + (i+1) + "_.jpg"
                                imageView2.setImageURI(uriList[i])
                                map["imageOfDetail2"] = imgNameList[i]
                            } else if (i == 2) {
                                imgNameList[i] = "IMAGE_" + timestamp + "-" + (i+1) + "_.jpg"
                                imageView3.setImageURI(uriList[i])
                                map["imageOfDetail3"] = imgNameList[i]
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

}
package kr.ac.cau.easyconnect

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ImageButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class WriteActivity : AppCompatActivity() {

    var firebaseAuth: FirebaseAuth? = null
    var storage : FirebaseStorage? = null

    val REQUEST_IMAGE_CAPTURE = 1
    val REQUEST_GALLERY_TAKE = 2

    var imgFileName : String? = null
    var uriPhoto : Uri? = null

    lateinit var currentPhotoPath : String
    lateinit var imageView : ImageView
    val timestamp: String = SimpleDateFormat("yyyyMMdd_HHmmsss").format(Date())

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write)

        // Firebase - Auth, Firestore의 인스턴스 받아오기
        firebaseAuth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        // xml id 연결
        val title: EditText = findViewById(R.id.title)
        val content: EditText = findViewById(R.id.content)

        imageView = findViewById(R.id.imageView)
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
        // 앨범버튼 눌렀을 때
        findViewById<Button>(R.id.album).setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_GALLERY_TAKE)
        }

        // 카메라 촬영 버튼 눌렀을 때
        findViewById<Button>(R.id.photoButton).setOnClickListener {
            takePicture()
        }

        // 게시버튼 눌렀을 때 구현
        findViewById<Button>(R.id.post).setOnClickListener {
            var inputTitle = title.text.trim().toString()
            var inputContent = content.text.trim().toString()
            var name = firebaseAuth!!.currentUser.email.toString()
            var registered : String = LocalDateTime.now().toString()
            var modified : String = LocalDateTime.now().toString()
            var imgOfDetail : String? = imgFileName

            val postDTO : PostDTO = PostDTO(inputTitle, inputContent, name, registered, modified, imgOfDetail)

            if (inputTitle.isNullOrEmpty()) {
                var builder = AlertDialog.Builder(this)
                builder.setTitle("제목을 입력해주세요.")
                builder.setPositiveButton("확인", null)
                builder.show()
            } else if (inputContent.isNullOrEmpty()) {
                var builder = AlertDialog.Builder(this)
                builder.setTitle("내용을 입력해주세요.")
                builder.setPositiveButton("확인", null)
                builder.show()
            } else {
                if (imgOfDetail != null)
                    imageUpload()
                db.collection("post").document(registered).set(postDTO).addOnCompleteListener(this) {
                    //글이 정상적으로 작성 됐을 때
                    if (it.isSuccessful) {
                        Toast.makeText(this, "success", Toast.LENGTH_SHORT).show()
                        //현재 엑티비티 종료하고 내가 쓴 글 확인하는 액티비티로 이동. 추후에 수정 예정
                        val intent = Intent(this, DetailActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    }
    // 뒤로가기 구현
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
        val timestamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
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

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

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
                    uriPhoto = Uri.fromFile(file)
                }
            }
            // 앨범에서 가져왔을 때
            REQUEST_GALLERY_TAKE -> {
                // Uri
                if (resultCode == Activity.RESULT_OK) {
                    uriPhoto = data?.data
                    imageView.setImageURI(uriPhoto) // handle chosen image
                    imgFileName = "IMAGE_" + timestamp + "_.jpg"
                }
            }
        }
    }

    private fun imageUpload() {
        var postDTO : PostDTO? = null

        // 결정되는대로 바꿀예정
        imgFileName = "IMAGE_" + timestamp + "_.jpg"

        storage = FirebaseStorage.getInstance()
        val db = FirebaseFirestore.getInstance()

        var riversRef = storage!!.reference.child("post").child(imgFileName!!)
        riversRef.putFile(uriPhoto!!).addOnSuccessListener {
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
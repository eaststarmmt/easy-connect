package kr.ac.cau.easyconnect

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.loader.content.CursorLoader
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.jar.Manifest

class Page_mypage : AppCompatActivity() {
    // 마이페이지 구현
    var firebaseAuth : FirebaseAuth? = null

    var storage : FirebaseStorage? = null
    var uriPhoto : Uri? = null
    val REQUEST_IMAGE_CAPTURE = 1
    val REQUEST_GALLERY_TAKE = 2
    lateinit var currentPhotoPath : String
    lateinit var imageView_me : ImageView
    var imgFileName : String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page_mypage)

        firebaseAuth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

//        val button_take_img : Button = findViewById(R.id.bt_take_img)            // 사진 찍는거 넣을지..
        val button_choose_img : Button = findViewById(R.id.bt_choose_img)
        val button_change_password : Button = findViewById(R.id.bt_change_password)
        val button_withdrawal : Button = findViewById(R.id.bt_withdrawal)
        val textView_id : TextView = findViewById(R.id.txt_id)
        val textView_phoneNumber : TextView = findViewById(R.id.txt_phoneNumber)
        val textView_name : TextView = findViewById(R.id.txt_name)
        imageView_me = findViewById(R.id.img_me)

        // 현재 정보를 기본적으로 출력
        var userDTO : UserDTO? = null
        db.collection("user_information").whereEqualTo("email", firebaseAuth!!.currentUser.email).get().addOnCompleteListener {
            if (it.isSuccessful) {
                // 현재 로그인 정보의 파이어스토어 데이터 조회
                storage = FirebaseStorage.getInstance()
                val storageReference = storage!!.reference

                for (dc in it.result!!.documents) {
                    userDTO = dc.toObject(UserDTO::class.java)
                    break
                }

                if (userDTO != null) {
                    imgFileName = userDTO!!.photo
                    textView_id.setText(userDTO!!.email)
                    textView_name.setText(userDTO!!.name)
                    textView_phoneNumber.setText(userDTO!!.phoneNumber)

                    var file: File? =
                        getExternalFilesDir(Environment.DIRECTORY_PICTURES + "/user_profile")
                    if (!file!!.isDirectory()) {
                        file!!.mkdir()
                    }

                    storageReference.child("user_profile/" + imgFileName).downloadUrl.addOnSuccessListener {
                        Glide.with(this /* context */)
                            .load(it)
                            .into(imageView_me)
                    }
                }
            }
        }


//        button_take_img.setOnClickListener({
//            // 이미지 촬영한 것으로 변경 기능
//            dispatchTakePictureIntent()
//        })

        button_choose_img.setOnClickListener({
            // 앨범에서 선택
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_GALLERY_TAKE)
        })

        button_change_password.setOnClickListener({
            // 비밀번호 변경 기능 완료

            var builder = AlertDialog.Builder(this)
            builder.setView(layoutInflater.inflate(R.layout.update_password_dialog, null))

            var listener = DialogInterface.OnClickListener { p0, _ ->
                var dialog = p0 as AlertDialog
                var input_change_password: EditText? =
                    dialog.findViewById(R.id.edit_change_password)
                var input_current_password: EditText? =
                    dialog.findViewById(R.id.edit_current_password)

                db.collection("user_information").whereEqualTo(
                    "email",
                    firebaseAuth!!.currentUser.email
                ).get().addOnCompleteListener {
                    if (it.isSuccessful) {
                        // 현재 로그인 정보의 파이어스토어 데이터 조회
                        var userDTO: UserDTO? = null
                        for (dc in it.result!!.documents) {
                            userDTO = dc.toObject(UserDTO::class.java)
                            break
                        }
                        if (userDTO != null) {
                            // 조회 성공 시
                            if (input_current_password!!.text.toString() == userDTO!!.password) {
                                // 현재 비밀번호가 접속중인 로그인 정보와 일치하는지 비교
                                // 일치한다면
                                firebaseAuth!!.currentUser.updatePassword(input_change_password!!.text.toString())
                                    .addOnCompleteListener(
                                        this
                                    ) {
                                        if (it.isSuccessful) {
                                            // 비밀번호 변경 성공
                                            Toast.makeText(this, "비밀번호 변경 성공!!", Toast.LENGTH_SHORT)
                                                .show()
                                            // 파이어스토어 변경
                                            var userPhoneNumber: String

                                            if (userDTO != null) {
                                                // 파이어스토어 업데이트!!
                                                userDTO!!.password =
                                                    input_change_password!!.text.toString()
                                                userPhoneNumber = userDTO!!.phoneNumber.toString()

                                                // 파이어스토어의 현재 회원 정보 삭제 및 추가 (업데이트)
                                                db.collection("user_information").document(
                                                    userPhoneNumber
                                                ).delete()
                                                db.collection("user_information").document(
                                                    userPhoneNumber
                                                ).set(userDTO!!)
                                            }
                                        }
                                    }
                            } else {
                                // 로그인 정보와 일치하지 않는다면
                                Toast.makeText(this, "변경 실패! 현재 비밀번호를 틀렸습니다.", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }
                }
            }
            builder.setPositiveButton("변경", listener)
            builder.setNegativeButton("취소", null)
            builder.show()
        })

        button_withdrawal.setOnClickListener({
            // 회원 탈퇴 기능 완료
            var builder = AlertDialog.Builder(this)
            builder.setView(layoutInflater.inflate(R.layout.withdrawal_dialog, null))

            var listener = DialogInterface.OnClickListener { p0, _ ->
                /////////////////////// 데이터베이스 firestore에서도 해당 회원 정보 삭제해야함!!!!!!!!!!!!!!!!!! 2021-04-15 04:39
                var dialog = p0 as AlertDialog
                var input_current_password2: EditText? =
                    dialog.findViewById(R.id.edit_current_password2)

                var userDTO: UserDTO? = null
                var userPhoneNumber: String
                db.collection("user_information").whereEqualTo(
                    "email",
                    firebaseAuth!!.currentUser.email
                ).get().addOnCompleteListener {
                    if (it.isSuccessful) {
                        for (dc in it.result!!.documents) {
                            userDTO = dc.toObject(UserDTO::class.java)
                            break
                        }
                        if (userDTO != null) {
                            if (input_current_password2!!.text.toString() == userDTO!!.password) {
                                // 비밀번호 일치
                                userPhoneNumber = userDTO!!.phoneNumber.toString()
                                // 파이어스토어의 현재 회원 정보 삭제
                                db.collection("user_information").document(userPhoneNumber).delete()

                                // 현재 로그인 정보 삭제
                                firebaseAuth!!.currentUser.delete()
                                Toast.makeText(this, "회원 탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT).show()

                                val intentLogin = Intent(this, Page_login::class.java)
                                startActivity(intentLogin)
                                finish()
                            } else {
                                Toast.makeText(this, "비밀번호가 틀렸습니다.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }

            builder.setPositiveButton("확인", listener)
            builder.setNegativeButton("취소", null)
            builder.show()
        })
    }

    // 카메라 촬영하는 방법!! 넣게 되면 사진 파일이 Storage에 빈파일로 올라가는 것 해결해야 함

//    // 카메라 열기
//    private fun dispatchTakePictureIntent() {
//        storage = FirebaseStorage.getInstance()
//        val db = FirebaseFirestore.getInstance()
//        var userDTO : UserDTO? = null
//
//        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
//            if (takePictureIntent.resolveActivity(this.packageManager) != null) {
//                // 찍은 사진을 그림파일로 만들기
//                val photoFile: File? =
//                    try {
//                        createImageFile()
//                    } catch (ex: IOException) {
//                        Log.d("TAG", "그림파일 만드는도중 에러생김")
//                        null
//                    }
//
//                // 그림파일을 성공적으로 만들었다면 onActivityForResult로 보내기
//                photoFile?.also {
//                    val photoUri: Uri = FileProvider.getUriForFile(
//                        this, "kr.ac.cau.easyconnect.fileprovider", it
//                    )
//
//                    imgFileName = photoFile.name
//                    val mediaScanIntent: Intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//                    // 해당 경로에 있는 파일을 객체화(새로 파일을 만든다는 것으로 이해하면 안 됨)
//                    val f: File = File(currentPhotoPath);
//                    val contentUri: Uri = Uri.fromFile(f);
//                    mediaScanIntent.setData(contentUri);
//                    sendBroadcast(mediaScanIntent);
//                    Toast.makeText(this, "사진이 앨범에 저장되었습니다.", Toast.LENGTH_SHORT).show();
//
//                    var storageRef = storage!!.reference.child("user_profile").child(imgFileName!!)
//                    storageRef.putFile(photoUri!!).addOnSuccessListener {
//                        storageRef.downloadUrl.addOnCompleteListener { uri ->
//                            db.collection("user_information")
//                                .whereEqualTo("email", firebaseAuth!!.currentUser.email).get()
//                                .addOnCompleteListener {
//                                    if (it.isSuccessful) {
//                                        for (dc in it.result!!.documents) {
//                                            userDTO = dc.toObject(UserDTO::class.java)
//                                            break
//                                        }
//                                        if (userDTO != null) {
//                                            userDTO!!.photo = imgFileName
//
//                                            db.collection("user_information").document(userDTO!!.phoneNumber.toString()).delete()
//                                            db.collection("user_information").document(userDTO!!.phoneNumber.toString()).set(
//                                                userDTO!!
//                                            )
//                                        }
//                                    }
//                                }
//                        }
//                    }
//
//                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
//                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
//                }
//            }
//        }
//    }
//
//    // 카메라로 촬영한 이미지를 파일로 저장해준다
//    @Throws(IOException::class)
//    private fun createImageFile(): File {
//        // Create an image file name
//        val timestamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
//        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
//        imgFileName = "IMAGE_" + timestamp + "_.jpg"
//
//        return File.createTempFile(
//            "JPEG_${timestamp}_", /* prefix */
//            ".jpg", /* suffix */
//            storageDir /* directory */
//        ).apply {
//            // Save a file: path for use with ACTION_VIEW intents
//            currentPhotoPath = absolutePath
//        }
//    }

    // 선택
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode){
            // 카메라로 촬영했을 때
//            REQUEST_IMAGE_CAPTURE -> {
//                if (resultCode == Activity.RESULT_OK) {
//                    // 카메라로부터 받은 데이터가 있을경우에만
//                    val file = File(currentPhotoPath)
//                    val decode = ImageDecoder.createSource(
//                        this.contentResolver,
//                        Uri.fromFile(file)
//                    )
//                    val bitmap = ImageDecoder.decodeBitmap(decode)
//                    imageView_me.setImageBitmap(bitmap)
//                }
//            }
            // 앨범에서 가져왔을 때
            REQUEST_GALLERY_TAKE -> {
                // Uri
                if (resultCode == Activity.RESULT_OK){
                    uriPhoto = data?.data
                    imageView_me.setImageURI(uriPhoto) // handle chosen image
                    imageUpload()
                }
            }
        }
    }

    // 앨범에서 가져온 정보로 데이터베이스에 저장
    fun imageUpload(){
        var userDTO : UserDTO? = null
        val timestamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        imgFileName = "IMAGE_" + timestamp + "_.jpg"

        storage = FirebaseStorage.getInstance()
        val db = FirebaseFirestore.getInstance()

        var riversRef = storage!!.reference.child("user_profile").child(imgFileName!!)

        riversRef.putFile(uriPhoto!!).addOnSuccessListener {
            riversRef.downloadUrl.addOnSuccessListener { uri ->
                db.collection("user_information")
                    .whereEqualTo("email", firebaseAuth!!.currentUser.email).get()
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            for (dc in it.result!!.documents) {
                                userDTO = dc.toObject(UserDTO::class.java)
                                break
                            }
                            if (userDTO != null) {
                                userDTO!!.photo = imgFileName

                                db.collection("user_information")
                                    .document(userDTO!!.phoneNumber.toString()).delete()
                                db.collection("user_information")
                                    .document(userDTO!!.phoneNumber.toString()).set(
                                        userDTO!!
                                    )
                            }
                        }
                    }
            }
        }

    }
}
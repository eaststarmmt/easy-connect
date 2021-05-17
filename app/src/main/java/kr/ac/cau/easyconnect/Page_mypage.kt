package kr.ac.cau.easyconnect

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.ImageDecoder
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.InputType
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

// UI만 손보면 될 듯!

class Page_mypage : AppCompatActivity() {
    // 마이페이지 구현

    var firebaseAuth : FirebaseAuth? = null
    var storage : FirebaseStorage? = null
    var db : FirebaseFirestore? = null

    var uriPhoto : Uri? = null
    var imgFileName : String? = null

    val REQUEST_IMAGE_CAPTURE = 1
    val REQUEST_GALLERY_TAKE = 2

    lateinit var currentPhotoPath : String
    lateinit var imageView_me : ImageButton
    var userDTO : UserDTO? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page_mypage)

        // 권한과 파이어스토어 데이터베이스 객체 받아옴
        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // xml 파일의 버튼과 텍스트, 그리고 이미지뷰에 연결!
        val button_goback : ImageButton = findViewById(R.id.bt_goback)
        val button_take_img : Button = findViewById(R.id.bt_camera)
        val button_choose_img : Button = findViewById(R.id.bt_choose_img)
        val button_change_info : Button = findViewById(R.id.bt_change_information)
        val button_change_password : Button = findViewById(R.id.bt_change_password)
        val button_withdrawal : Button = findViewById(R.id.bt_withdrawal)
        val textView_phoneNumber : TextView = findViewById(R.id.txt_phoneNumber)
        val textView_name : TextView = findViewById(R.id.txt_name)
        val switchSearch : Switch = findViewById(R.id.switch_search)
        imageView_me = findViewById(R.id.img_me)

        val sharedPreference = getSharedPreferences("searchBoolean", 0)
        switchSearch.isChecked = sharedPreference.getBoolean("searchState", false)

        switchSearch.setOnCheckedChangeListener(searchSwitchListener())

        // 현재 정보를 기본적으로 출력하는 부분!
        db!!.collection("user_information").whereEqualTo("email", firebaseAuth!!.currentUser.email).get().addOnCompleteListener {
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
                    textView_name.setText(userDTO!!.name)
                    textView_phoneNumber.setText(userDTO!!.phoneNumber)

                    // 해당 폴더("user_profile")가 존재하지 않는다면 생성
                    var file: File? =
                        getExternalFilesDir(Environment.DIRECTORY_PICTURES + "/user_profile")
                    if (!file!!.isDirectory()) {
                        file!!.mkdir()
                    }

                    // Url을 참조하여 해당 경로의 이미지를 읽어와서 Glide를 사용해 이미지뷰에 띄워주는 역할
                    storageReference.child("user_profile/" + imgFileName).downloadUrl.addOnSuccessListener {
                        Glide.with(this /* context */)
                            .load(it)
                            .into(imageView_me)
                    }

                    imageView_me.setBackground(ShapeDrawable(OvalShape()))
                    imageView_me.setClipToOutline(true)
                }
            }
        }

        imageView_me.setOnClickListener({
            val intentImageMe = Intent(this, Page_imageme::class.java).apply{
                val userPhoto = userDTO!!.photo
                putExtra("userPhoto", userPhoto)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intentImageMe)
        })

        button_goback.setOnClickListener({
            val intentMenu = Intent(this, Page_menu::class.java)
            startActivity(intentMenu)
            finish()
        })

        button_take_img.setOnClickListener({
            // 사진을 직접 촬영하기!
            dispatchTakePictureIntent()
        })

        button_choose_img.setOnClickListener({
            // 앨범에서 선택하기!
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_GALLERY_TAKE)
        })

        button_change_info.setOnClickListener({
            // 이름이나 휴대폰 번호 변경하기!
            // 기본 정보를 그대로 띄워주고 변경할거면 입력하게끔!

            var builder = AlertDialog.Builder(this)
            builder.setView(layoutInflater.inflate(R.layout.update_information_dialog, null))

            var listener = DialogInterface.OnClickListener { p0, _ ->
                var dialog = p0 as AlertDialog
                var input_change_name : EditText? = dialog.findViewById(R.id.edit_change_name)
                var input_change_phoneNumber: EditText? = dialog.findViewById(R.id.edit_change_phoneNumber)

                // 파이어스토어 업데이트!!
                if(userDTO != null){
                    if(input_change_name!!.text.toString().isNullOrEmpty()){
                        if(input_change_phoneNumber!!.text.toString().isNullOrEmpty()){
                            // 둘다 비어있음 -> 기존정보 출력
                        }else {
                            // 이름만 비어있음 -> 전화번호만 변경 (단 전화번호가 10자리 미만이면 입력 오류로 받아들일 것!)
                            if (input_change_phoneNumber!!.text.toString().length < 10) {
                                Toast.makeText(this, "10자리 이상의 전화번호를 입력하세요.", Toast.LENGTH_SHORT).show()
                            } else {
                                userDTO!!.phoneNumber = input_change_phoneNumber!!.text.toString()
                            }
                        }
                    }else{
                        if(input_change_phoneNumber!!.text.toString().isNullOrEmpty()){
                            // 전화번호만 비어있음 -> 이름만 변경
                            userDTO!!.name = input_change_name!!.text.toString()
                        }else{
                            // 둘 다 비어있지 않음 -> 둘 다 변경 (단 전화번호가 10자리 미만이면 입력 오류로 받아들일 것!)
                            if (input_change_phoneNumber!!.text.toString().length < 10) {
                                Toast.makeText(this, "10자리 이상의 전화번호를 입력하세요.", Toast.LENGTH_SHORT).show()
                            } else {
                                userDTO!!.name = input_change_name!!.text.toString()
                                userDTO!!.phoneNumber = input_change_phoneNumber!!.text.toString()
                            }
                        }
                    }

                    // 파이어스토어의 현재 회원 정보 삭제 및 추가 (업데이트)
                    db!!.collection("user_information").document(
                            userDTO!!.uid.toString()
                    ).delete()
                    db!!.collection("user_information").document(
                            userDTO!!.uid.toString()
                    ).set(userDTO!!)

                    // 변경된 정보로 다시 mypage 정보 바꿔주기
                    textView_name.setText(userDTO!!.name)
                    textView_phoneNumber.setText(userDTO!!.phoneNumber)
                }
            }

            builder.setPositiveButton("변경", listener)
            builder.setNegativeButton("취소", null)
            builder.show()
        })

        button_change_password.setOnClickListener({
            // 비밀번호 변경 기능
            var builder = AlertDialog.Builder(this)
            var passwordView : View? = layoutInflater.inflate(R.layout.update_password_dialog, null)
            var input_current_password : EditText? = passwordView!!.findViewById(R.id.edit_current_password)!!
            var input_change_password : EditText? = passwordView!!.findViewById(R.id.edit_change_password)!!
            var checkbox_current : CheckBox? = passwordView!!.findViewById(R.id.checkbox_pw_current)!!
            var checkbox_changed : CheckBox? = passwordView!!.findViewById(R.id.checkbox_pw_changed)!!
            builder.setView(passwordView)

            checkbox_current!!.setOnCheckedChangeListener{ buttonView, isChecked ->
                if(isChecked){
                    input_current_password!!.transformationMethod = HideReturnsTransformationMethod.getInstance()
                }else{
                    input_current_password!!.transformationMethod = PasswordTransformationMethod.getInstance()
                }
            }
            checkbox_changed!!.setOnCheckedChangeListener{ buttonView, isChecked ->
                if(isChecked){
                    input_change_password!!.transformationMethod = HideReturnsTransformationMethod.getInstance()
                }else{
                    input_change_password!!.transformationMethod = PasswordTransformationMethod.getInstance()
                }
            }

            var listener = DialogInterface.OnClickListener { p0, _ ->
                // 현재 접속중인 정보는 이미 화면을 띄울 때 userDTO 객체에 담아두었으니 비교만 하면 된다.
                if (userDTO != null) {
                    // 조회 성공 시
                    if (input_current_password!!.text.toString() == userDTO!!.password) {
                        // 입력한 현재 비밀번호가 접속중인 계정의 비밀번호와 일치하는지 비교
                        if (input_change_password!!.text.toString().isNullOrEmpty() || input_change_password.length() < 6){
                            // 새 비밀번호 입력하지 않음
                            Toast.makeText(this, "새 비밀번호를 입력하세요", Toast.LENGTH_SHORT)
                                .show()
                        }
                        else if(input_change_password.length() < 6){
                            // 비밀번호가 너무 짧음
                            Toast.makeText(this, "비밀번호를 6자리 이상으로 입력하세요", Toast.LENGTH_SHORT)
                                .show()
                        }
                        else{
                            firebaseAuth!!.currentUser.updatePassword(input_change_password!!.text.toString())
                                .addOnCompleteListener(
                                    this
                                ) {
                                    // 일치하므로 새로 입력한 비밀번호로 변경해 줌
                                    if (it.isSuccessful) {
                                        // 비밀번호 변경 성공
                                        Toast.makeText(this, "비밀번호 변경 성공!!", Toast.LENGTH_SHORT)
                                            .show()
                                        // 파이어스토어 변경
                                        if (userDTO != null) {
                                            // 파이어스토어 업데이트!!
                                            userDTO!!.password =
                                                input_change_password!!.text.toString()

                                            // 파이어스토어의 현재 회원 정보 삭제 및 추가 (업데이트)
                                            db!!.collection("user_information").document(
                                                userDTO!!.uid.toString()
                                            ).delete()
                                            db!!.collection("user_information").document(
                                                userDTO!!.uid.toString()
                                            ).set(userDTO!!)
                                        }
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
            builder.setPositiveButton("변경", listener)
            builder.setNegativeButton("취소", null)
            builder.show()
        })

        button_withdrawal.setOnClickListener({
            // 회원 탈퇴 기능 완료
            var builder = AlertDialog.Builder(this)
            builder.setView(layoutInflater.inflate(R.layout.withdrawal_dialog, null))

            var listener = DialogInterface.OnClickListener { p0, _ ->
                var dialog = p0 as AlertDialog
                var input_current_password2: EditText? =
                        dialog.findViewById(R.id.edit_current_password2)

                // 현재 접속중인 정보는 이미 화면을 띄울 때 userDTO 객체에 담아두었으니 비교만 하면 된다.
                if (userDTO != null) {
                    if (input_current_password2!!.text.toString() == userDTO!!.password) {
                        // 현재 사용중인 비밀번호와 접속중인 계정의 비밀번호가 일치
                        // 파이어스토어의 현재 회원 정보 삭제
                        db!!.collection("user_information").document(userDTO!!.uid.toString()).delete()

                        // 현재 로그인 정보 삭제
                        firebaseAuth!!.currentUser.delete()
                        Toast.makeText(this, "회원 탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT).show()

                        // 회원 탈퇴가 완료되었으니 로그인 페이지로 되돌아감
                        val intentLogin = Intent(this, Page_login::class.java)
                        startActivity(intentLogin)
                        finish()
                    } else {
                        Toast.makeText(this, "비밀번호가 틀렸습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            builder.setPositiveButton("확인", listener)
            builder.setNegativeButton("취소", null)
            builder.show()
        })
    }

    inner class searchSwitchListener : CompoundButton.OnCheckedChangeListener {

        override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
            if (isChecked){
                if (userDTO != null) {
                    userDTO!!.search = true
                    val sharedPreference = getSharedPreferences("searchBoolean", 0)
                    val editor = sharedPreference.edit()
                    editor.putBoolean("searchState", userDTO!!.search!!)
                    editor.apply()
                }
            }
            else{
                if (userDTO != null) {
                    userDTO!!.search = false
                    val sharedPreference = getSharedPreferences("searchBoolean", 0)
                    val editor = sharedPreference.edit()
                    editor.putBoolean("searchState", userDTO!!.search!!)
                    editor.apply()
                }
            }
            // 파이어스토어 갱신
            db!!.collection("user_information")
                .document(userDTO!!.uid.toString()).delete()
            db!!.collection("user_information")
                .document(userDTO!!.uid.toString()).set(
                    userDTO!!
                )
        }
    }

    // 카메라 사용!!
    private fun dispatchTakePictureIntent() {
        storage = FirebaseStorage.getInstance()

        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            if (takePictureIntent.resolveActivity(this.packageManager) != null) {
                // 찍은 사진을 그림파일로 만들기
                val photoFile: File? =
                    try {
                        createImageFile()
                    } catch (ex: IOException) {
                        Log.d("TAG", "그림파일 만드는도중 에러생김")
                        null
                    }

                // 그림파일을 성공적으로 만들었다면 onActivityForResult로 보내기
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

    // 카메라로 촬영한 이미지를 파일로 저장해준다 (이름은 현재 시간 정보를 반영해서 구분 가능하게)
    @Throws(IOException::class)
    private fun createImageFile(): File {
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

    // 선택 (REQUEST_IMAGE_CAPTURE / REQUEST_GALLERY_TAKE)
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode){
            // 카메라로 촬영했을 때
            REQUEST_IMAGE_CAPTURE -> {
                if (resultCode == Activity.RESULT_OK) {
                    // 카메라로부터 받은 데이터가 있을경우에만
                    val file = File(currentPhotoPath)
                    val decode = ImageDecoder.createSource(
                            this.contentResolver,
                            Uri.fromFile(file)
                    )
                    val bitmap = ImageDecoder.decodeBitmap(decode)
                    imageView_me.setImageBitmap(bitmap)
                    uriPhoto = Uri.fromFile(file)
                    imageUpload()
                }
            }
            // 앨범에서 가져왔을 때
            REQUEST_GALLERY_TAKE -> {
                // Uri
                if (resultCode == Activity.RESULT_OK) {
                    uriPhoto = data?.data
                    imageView_me.setImageURI(uriPhoto) // handle chosen image
                    imageUpload()
                }
            }
        }
    }

    // 가져온 URI 정보로 데이터베이스(파이어베이스 스토리지 / 파이어스토어)에 저장
    fun imageUpload(){
        var userDTO : UserDTO? = null
        val timestamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        imgFileName = "IMAGE_" + timestamp + "_.jpg"

        storage = FirebaseStorage.getInstance()
        val db = FirebaseFirestore.getInstance()

        // url 찾기 위해 참조!
        var riversRef = storage!!.reference.child("user_profile").child(imgFileName!!)
        riversRef.putFile(uriPhoto!!).addOnSuccessListener {
            riversRef.downloadUrl.addOnSuccessListener { uri ->
                // 현재 접속중인 계정의 email 정보를 바탕으로 데이터베이스 조회
                db.collection("user_information")
                    .whereEqualTo("email", firebaseAuth!!.currentUser.email).get()
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            for (dc in it.result!!.documents) {
                                userDTO = dc.toObject(UserDTO::class.java)
                                break
                            }
                            if (userDTO != null) {
                                // 현재 접속중인 계정의 데이터베이스 - photo 필드의 값을 현재 사진의 경로로 저장

                                // 파이어베이스 스토리지 정보 갱신해야함 (기존 정보 삭제 필요 - 이미지 누적되서 서버 과부하 걸릴 듯)
                                // 하지만 base파일이면 삭제 x!!!!!
                                if(userDTO!!.photo != "base.jpg") {
                                    val desertRef = storage!!.reference.child("user_profile/" + userDTO!!.photo)

                                    // Delete the file
                                    desertRef.delete().addOnSuccessListener {
                                        // File deleted successfully
                                    }.addOnFailureListener {
                                        // Uh-oh, an error occurred!
                                    }
                                }else{
                                    // base.jpg 파일!!
                                }

                                userDTO!!.photo = imgFileName

                                // 파이어스토어 갱신
                                db.collection("user_information")
                                    .document(userDTO!!.uid.toString()).delete()
                                db.collection("user_information")
                                    .document(userDTO!!.uid.toString()).set(
                                                userDTO!!
                                        )
                            }
                        }
                    }
            }
        }
    }

    override fun onBackPressed(){
        // 클릭 시 이전 페이지인 메인으로!
        val intentMenu = Intent(this, Page_menu::class.java)
        startActivity(intentMenu)
        finish()
    }
}
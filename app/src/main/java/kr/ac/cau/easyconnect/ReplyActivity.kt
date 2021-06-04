package kr.ac.cau.easyconnect

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Rect
import android.hardware.input.InputManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.getSystemService
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

import java.text.SimpleDateFormat

class ReplyActivity : AppCompatActivity() {

    var storage: FirebaseStorage? = null
    var firebaseAuth: FirebaseAuth? = null
    var db: FirebaseFirestore? = null
    var id : String? = null
    var userData : UserDTO? = null
    var replyContent : EditText? = null
    var emoticonContainer : ScrollView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //binding = ActivityReplyBinding.inflate(layoutInflater)
        //val view = binding.root
        setContentView(R.layout.activity_reply)

        storage = FirebaseStorage.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        id = intent.getStringExtra("id")  // 수정일 인텐트로 넘겨 받음
        val replyRecyclerview : RecyclerView = findViewById(R.id.reply_recyclerview)
        replyContent = findViewById(R.id.reply_content)

        // 액티비티 실행 시 키보드 바로 올려줌
        // replyContent!!.requestFocus()
        var keyBoardHeight = 0

        // 어댑터 연결 해야 나옴
        replyRecyclerview.adapter = ReplyAdapter()
        replyRecyclerview.layoutManager = LinearLayoutManager(this)
        //댓글 등록시
        findViewById<Button>(R.id.reply_button).setOnClickListener {

            var inputReply = replyContent!!.text.trim().toString()
            var name = firebaseAuth!!.currentUser.email
            // 현재 시간 출력
            val currentDateTime : Long  = System.currentTimeMillis()
            var registered : Long = System.currentTimeMillis()
            var modified : String = SimpleDateFormat("MM월dd일 HH:mm:ss").format(currentDateTime)


            val replyDTO : ReplyDTO = ReplyDTO(inputReply, name, registered, modified)

            if (inputReply.isNullOrEmpty()) {
                var builder = AlertDialog.Builder(this)
                builder.setTitle("내용을 입력해주세요.")
                builder.setPositiveButton("확인", null)
                builder.show()
            } else {
                db!!.collection("post/" + id + "/reply").document(registered.toString()).set(replyDTO).addOnCompleteListener(this) {
                    //글이 정상적으로 작성 됐을 때
                    if (it.isSuccessful) {
                        Toast.makeText(this, "완료", Toast.LENGTH_SHORT).show()
                        hideKeyboard()
                        replyContent!!.text = null
                    } else {
                        Toast.makeText(this, "failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }

        }
        var rootHeight = -1
        val rootView = findViewById<LinearLayout>(R.id.root_view)
        emoticonContainer = findViewById(R.id.emoticonContainer)

        // 키보드 높이만큼 이모티콘 위치 시키기 위해 값 측정
        rootView.viewTreeObserver.addOnGlobalLayoutListener {

            if (rootHeight == -1) rootHeight = rootView.height
            val visibleFrameSize = Rect()
            rootView.getWindowVisibleDisplayFrame(visibleFrameSize)
            val heightExceptKeyboard = visibleFrameSize.bottom - visibleFrameSize.top
            // 키보드를 제외한 높이가 디바이스 rootView보다 높거나 같으면 키보드가 올라왔을때가 아니므로 무시
            if (heightExceptKeyboard < rootHeight) {
                keyBoardHeight = rootHeight - heightExceptKeyboard  // 키보드 높이
                emoticonContainer!!.layoutParams.height = keyBoardHeight  // 이모티콘 컨테이너에 키보드 높이 입력
            }
        }
        // 이모티콘 버튼 눌렀을 때
        findViewById<Button>(R.id.emoticonButton).setOnClickListener {

            // 키보드에 따라 딸려오는거 해제
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
            // 시간차 때문에 딜레이 주고 이모티콘 컨테이너 보이게 함
            Handler().postDelayed({
                emoticonContainer!!.visibility = View.VISIBLE
            }, 90)

            hideKeyboard()

            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
        // 배경 클릭시 키보드 없애기
        rootView.setOnClickListener{
            if (emoticonContainer!!.visibility == View.VISIBLE) hideEmoticon()
            hideKeyboard()
        }
        // 댓글 입력창 클릭시 이모티콘 없애기
        replyContent!!.setOnClickListener {
            if (emoticonContainer!!.visibility == View.VISIBLE) hideEmoticon()
        }

        // 이모티콘 사용시 바로 등록되게 함. 일단 하드코딩으로 처리.
        // 놀람 이모티콘 클릭
        findViewById<ImageView>(R.id.amazing).setOnClickListener {
            var inputReply = null
            var name = firebaseAuth!!.currentUser.email
            // 현재 시간 출력
            val currentDateTime : Long  = System.currentTimeMillis()
            var registered : Long = System.currentTimeMillis()
            var modified : String = SimpleDateFormat("MM월dd일 HH:mm:ss").format(currentDateTime)
            val emoticon : String = "amazing"

            val replyDTO : ReplyDTO = ReplyDTO(inputReply, name, registered, modified, emoticon)
            db!!.collection("post/" + id + "/reply").document(registered.toString()).set(replyDTO).addOnCompleteListener(this) {
                //글이 정상적으로 작성 됐을 때
                if (it.isSuccessful) {
                    Toast.makeText(this, "작성이 완료되었습니다", Toast.LENGTH_SHORT).show()
                    hideKeyboard()
                    hideEmoticon()
                    replyContent!!.text = null
                } else {
                    Toast.makeText(this, "failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
        // 화났소 이모티콘 클릭
        findViewById<ImageView>(R.id.angry).setOnClickListener {
            var inputReply = null
            var name = firebaseAuth!!.currentUser.email
            // 현재 시간 출력
            val currentDateTime : Long  = System.currentTimeMillis()
            var registered : Long = System.currentTimeMillis()
            var modified : String = SimpleDateFormat("MM월dd일 HH:mm:ss").format(currentDateTime)
            val emoticon : String = "angry"

            val replyDTO : ReplyDTO = ReplyDTO(inputReply, name, registered, modified, emoticon)
            db!!.collection("post/" + id + "/reply").document(registered.toString()).set(replyDTO).addOnCompleteListener(this) {
                //글이 정상적으로 작성 됐을 때
                if (it.isSuccessful) {
                    Toast.makeText(this, "작성이 완료되었습니다", Toast.LENGTH_SHORT).show()
                    hideKeyboard()
                    hideEmoticon()
                    replyContent!!.text = null
                } else {
                    Toast.makeText(this, "failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
        // 울고싶소 이모티콘 클릭
        findViewById<ImageView>(R.id.cry).setOnClickListener {
            var inputReply = null
            var name = firebaseAuth!!.currentUser.email
            // 현재 시간 출력
            val currentDateTime : Long  = System.currentTimeMillis()
            var registered : Long = System.currentTimeMillis()
            var modified : String = SimpleDateFormat("MM월dd일 HH:mm:ss").format(currentDateTime)
            val emoticon : String = "cry"

            val replyDTO : ReplyDTO = ReplyDTO(inputReply, name, registered, modified, emoticon)
            db!!.collection("post/" + id + "/reply").document(registered.toString()).set(replyDTO).addOnCompleteListener(this) {
                //글이 정상적으로 작성 됐을 때
                if (it.isSuccessful) {
                    Toast.makeText(this, "작성이 완료되었습니다", Toast.LENGTH_SHORT).show()
                    hideKeyboard()
                    hideEmoticon()
                    replyContent!!.text = null
                } else {
                    Toast.makeText(this, "failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
        // 안녕하소 이모티콘 클릭
        findViewById<ImageView>(R.id.hello).setOnClickListener {
            var inputReply = null
            var name = firebaseAuth!!.currentUser.email
            // 현재 시간 출력
            val currentDateTime : Long  = System.currentTimeMillis()
            var registered : Long = System.currentTimeMillis()
            var modified : String = SimpleDateFormat("MM월dd일 HH:mm:ss").format(currentDateTime)
            val emoticon : String = "hello"

            val replyDTO : ReplyDTO = ReplyDTO(inputReply, name, registered, modified, emoticon)
            db!!.collection("post/" + id + "/reply").document(registered.toString()).set(replyDTO).addOnCompleteListener(this) {
                //글이 정상적으로 작성 됐을 때
                if (it.isSuccessful) {
                    Toast.makeText(this, "작성이 완료되었습니다", Toast.LENGTH_SHORT).show()
                    hideKeyboard()
                    hideEmoticon()
                    replyContent!!.text = null
                } else {
                    Toast.makeText(this, "failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 좋소 이모티콘 클릭
        findViewById<ImageView>(R.id.good).setOnClickListener {
            var inputReply = null
            var name = firebaseAuth!!.currentUser.email
            // 현재 시간 출력
            val currentDateTime : Long  = System.currentTimeMillis()
            var registered : Long = System.currentTimeMillis()
            var modified : String = SimpleDateFormat("MM월dd일 HH:mm:ss").format(currentDateTime)
            val emoticon : String = "good"

            val replyDTO : ReplyDTO = ReplyDTO(inputReply, name, registered, modified, emoticon)
            db!!.collection("post/" + id + "/reply").document(registered.toString()).set(replyDTO).addOnCompleteListener(this) {
                //글이 정상적으로 작성 됐을 때
                if (it.isSuccessful) {
                    Toast.makeText(this, "작성이 완료되었습니다", Toast.LENGTH_SHORT).show()
                    hideKeyboard()
                    hideEmoticon()
                    replyContent!!.text = null
                } else {
                    Toast.makeText(this, "failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 좋아 이모티콘 클릭
        findViewById<ImageView>(R.id.great).setOnClickListener {
            var inputReply = null
            var name = firebaseAuth!!.currentUser.email
            // 현재 시간 출력
            val currentDateTime : Long  = System.currentTimeMillis()
            var registered : Long = System.currentTimeMillis()
            var modified : String = SimpleDateFormat("MM월dd일 HH:mm:ss").format(currentDateTime)
            val emoticon : String = "great"

            val replyDTO : ReplyDTO = ReplyDTO(inputReply, name, registered, modified, emoticon)
            db!!.collection("post/" + id + "/reply").document(registered.toString()).set(replyDTO).addOnCompleteListener(this) {
                //글이 정상적으로 작성 됐을 때
                if (it.isSuccessful) {
                    Toast.makeText(this, "작성이 완료되었습니다", Toast.LENGTH_SHORT).show()
                    hideKeyboard()
                    hideEmoticon()
                    replyContent!!.text = null
                } else {
                    Toast.makeText(this, "failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 궁금 이모티콘 클릭
        findViewById<ImageView>(R.id.curiosity).setOnClickListener {
            var inputReply = null
            var name = firebaseAuth!!.currentUser.email
            // 현재 시간 출력
            val currentDateTime : Long  = System.currentTimeMillis()
            var registered : Long = System.currentTimeMillis()
            var modified : String = SimpleDateFormat("MM월dd일 HH:mm:ss").format(currentDateTime)
            val emoticon : String = "curiosity"

            val replyDTO : ReplyDTO = ReplyDTO(inputReply, name, registered, modified, emoticon)
            db!!.collection("post/" + id + "/reply").document(registered.toString()).set(replyDTO).addOnCompleteListener(this) {
                //글이 정상적으로 작성 됐을 때
                if (it.isSuccessful) {
                    Toast.makeText(this, "작성이 완료되었습니다", Toast.LENGTH_SHORT).show()
                    hideKeyboard()
                    hideEmoticon()
                    replyContent!!.text = null
                } else {
                    Toast.makeText(this, "failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 안녕 이모티콘 클릭
        findViewById<ImageView>(R.id.hi).setOnClickListener {
            var inputReply = null
            var name = firebaseAuth!!.currentUser.email
            // 현재 시간 출력
            val currentDateTime : Long  = System.currentTimeMillis()
            var registered : Long = System.currentTimeMillis()
            var modified : String = SimpleDateFormat("MM월dd일 HH:mm:ss").format(currentDateTime)
            val emoticon : String = "hi"

            val replyDTO : ReplyDTO = ReplyDTO(inputReply, name, registered, modified, emoticon)
            db!!.collection("post/" + id + "/reply").document(registered.toString()).set(replyDTO).addOnCompleteListener(this) {
                //글이 정상적으로 작성 됐을 때
                if (it.isSuccessful) {
                    Toast.makeText(this, "작성이 완료되었습니다", Toast.LENGTH_SHORT).show()
                    hideKeyboard()
                    hideEmoticon()
                    replyContent!!.text = null
                } else {
                    Toast.makeText(this, "failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 사랑 이모티콘 클릭
        findViewById<ImageView>(R.id.love).setOnClickListener {
            var inputReply = null
            var name = firebaseAuth!!.currentUser.email
            // 현재 시간 출력
            val currentDateTime : Long  = System.currentTimeMillis()
            var registered : Long = System.currentTimeMillis()
            var modified : String = SimpleDateFormat("MM월dd일 HH:mm:ss").format(currentDateTime)
            val emoticon : String = "love"

            val replyDTO : ReplyDTO = ReplyDTO(inputReply, name, registered, modified, emoticon)
            db!!.collection("post/" + id + "/reply").document(registered.toString()).set(replyDTO).addOnCompleteListener(this) {
                //글이 정상적으로 작성 됐을 때
                if (it.isSuccessful) {
                    Toast.makeText(this, "작성이 완료되었습니다", Toast.LENGTH_SHORT).show()
                    hideKeyboard()
                    hideEmoticon()
                    replyContent!!.text = null
                } else {
                    Toast.makeText(this, "failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 멋지소 이모티콘 클릭
        findViewById<ImageView>(R.id.nice).setOnClickListener {
            var inputReply = null
            var name = firebaseAuth!!.currentUser.email
            // 현재 시간 출력
            val currentDateTime : Long  = System.currentTimeMillis()
            var registered : Long = System.currentTimeMillis()
            var modified : String = SimpleDateFormat("MM월dd일 HH:mm:ss").format(currentDateTime)
            val emoticon : String = "nice"

            val replyDTO : ReplyDTO = ReplyDTO(inputReply, name, registered, modified, emoticon)
            db!!.collection("post/" + id + "/reply").document(registered.toString()).set(replyDTO).addOnCompleteListener(this) {
                //글이 정상적으로 작성 됐을 때
                if (it.isSuccessful) {
                    Toast.makeText(this, "작성이 완료되었습니다", Toast.LENGTH_SHORT).show()
                    hideKeyboard()
                    hideEmoticon()
                    replyContent!!.text = null
                } else {
                    Toast.makeText(this, "failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
        // 슬퍼 이모티콘 클릭
        findViewById<ImageView>(R.id.sad).setOnClickListener {
            var inputReply = null
            var name = firebaseAuth!!.currentUser.email
            // 현재 시간 출력
            val currentDateTime : Long  = System.currentTimeMillis()
            var registered : Long = System.currentTimeMillis()
            var modified : String = SimpleDateFormat("MM월dd일 HH:mm:ss").format(currentDateTime)
            val emoticon : String = "sad"

            val replyDTO : ReplyDTO = ReplyDTO(inputReply, name, registered, modified, emoticon)
            db!!.collection("post/" + id + "/reply").document(registered.toString()).set(replyDTO).addOnCompleteListener(this) {
                //글이 정상적으로 작성 됐을 때
                if (it.isSuccessful) {
                    Toast.makeText(this, "작성이 완료되었습니다", Toast.LENGTH_SHORT).show()
                    hideKeyboard()
                    hideEmoticon()
                    replyContent!!.text = null
                } else {
                    Toast.makeText(this, "failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 화나 이모티콘 클릭
        findViewById<ImageView>(R.id.upset).setOnClickListener {
            var inputReply = null
            var name = firebaseAuth!!.currentUser.email
            // 현재 시간 출력
            val currentDateTime : Long  = System.currentTimeMillis()
            var registered : Long = System.currentTimeMillis()
            var modified : String = SimpleDateFormat("MM월dd일 HH:mm:ss").format(currentDateTime)
            val emoticon : String = "upset"

            val replyDTO : ReplyDTO = ReplyDTO(inputReply, name, registered, modified, emoticon)
            db!!.collection("post/" + id + "/reply").document(registered.toString()).set(replyDTO).addOnCompleteListener(this) {
                //글이 정상적으로 작성 됐을 때
                if (it.isSuccessful) {
                    Toast.makeText(this, "작성이 완료되었습니다", Toast.LENGTH_SHORT).show()
                    hideKeyboard()
                    hideEmoticon()
                    replyContent!!.text = null
                } else {
                    Toast.makeText(this, "failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

        //추천
        findViewById<Button>(R.id.recommendation).setOnClickListener {
            hideKeyboard()
            hideEmoticon()
            var recomList : MutableList<String> = mutableListOf()
            val recomContainer : ScrollView = findViewById(R.id.recomContainer)
            val recom1 : ImageView = findViewById(R.id.recom1)
            val recom2 : ImageView = findViewById(R.id.recom2)
            val recom3 : ImageView = findViewById(R.id.recom3)
            val recom4 : ImageView = findViewById(R.id.recom4)
            val recom5 : ImageView = findViewById(R.id.recom5)
            val recom6 : ImageView = findViewById(R.id.recom6)
            val recom7 : ImageView = findViewById(R.id.recom7)
            val recom8 : ImageView = findViewById(R.id.recom8)
            val recom9 : ImageView = findViewById(R.id.recom9)
            val recom10 : ImageView = findViewById(R.id.recom10)
            val recom11 : ImageView = findViewById(R.id.recom11)
            val recom12 : ImageView = findViewById(R.id.recom12)
            recomContainer.visibility = View.VISIBLE
            if (replyContent!!.text.contains("깜짝") || replyContent!!.text.contains("놀랍") ||
                replyContent!!.text.contains("놀람") || replyContent!!.text.contains("놀라")) {
                recomList.add("amazing")
            } else if (replyContent!!.text.contains("짜증") || replyContent!!.text.contains("화") ||
                replyContent!!.text.contains("열받") || replyContent!!.text.contains("빡")) {
                recomList.add("angry")
                recomList.add("upset")
            } else if (replyContent!!.text.contains("슬퍼") || replyContent!!.text.contains("울고") ||
                replyContent!!.text.contains("울거") || replyContent!!.text.contains("힘들")) {
                recomList.add("cry")
                recomList.add("sad")
            } else if (replyContent!!.text.contains("안녕") || replyContent!!.text.contains("반가")
                || replyContent!!.text.contains("방가") || replyContent!!.text.contains("헬로")
            || replyContent!!.text.contains("하이") ){
                recomList.add("hello")
                recomList.add("hi")
            } else if (replyContent!!.text.contains("좋") || replyContent!!.text.contains("잘했") || replyContent!!.text.contains("죽이네") ||
                replyContent!!.text.contains("잘됐") || replyContent!!.text.contains("잘 됐")) {
                recomList.add("good")
                recomList.add("great")
                recomList.add("good")
            } else if (replyContent!!.text.contains("궁금") || replyContent!!.text.contains("뭐")
                || replyContent!!.text.contains("뭔") || replyContent!!.text.contains("왜") || replyContent!!.text.contains("?")) {
                recomList.add("curiosity")
            } else if (replyContent!!.text.contains("사랑") || replyContent!!.text.contains("좋아")) {
                recomList.add("love")
            }
            var i = 1
            for (emo in recomList) {
                var recom : ImageView = findViewById<ImageView>(resources.getIdentifier("recom" + i, "id", "kr.ac.cau.easyconnect"))
                recom.setOnClickListener {
                    var inputReply = null
                    var name = firebaseAuth!!.currentUser.email
                    // 현재 시간 출력
                    val currentDateTime : Long  = System.currentTimeMillis()
                    var registered : Long = System.currentTimeMillis()
                    var modified : String = SimpleDateFormat("MM월dd일 HH:mm:ss").format(currentDateTime)
                    val emoticon : String = emo
                    val replyDTO : ReplyDTO = ReplyDTO(inputReply, name, registered, modified, emoticon)
                    db!!.collection("post/" + id + "/reply").document(registered.toString()).set(replyDTO).addOnCompleteListener(this) {
                        //글이 정상적으로 작성 됐을 때
                        if (it.isSuccessful) {
                            Toast.makeText(this, "작성이 완료되었습니다", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                recom.visibility = View.VISIBLE
                when (emo) {
                    "amazing" -> recom.setImageResource(R.drawable.emo_amazing)
                    "angry" -> recom.setImageResource(R.drawable.emo_angry)
                    "cry" -> recom.setImageResource(R.drawable.emo_cry)
                    "hello" -> recom.setImageResource(R.drawable.emo_hello)
                    "good" -> recom.setImageResource(R.drawable.emo_good)
                    "great" -> recom.setImageResource(R.drawable.emo_great)
                    "curiosity" -> recom.setImageResource(R.drawable.emo_curiosity)
                    "hi" -> recom.setImageResource(R.drawable.emo_hi)
                    "love" -> recom.setImageResource(R.drawable.emo_love)
                    "nice" -> recom.setImageResource(R.drawable.emo_nice)
                    "sad" -> recom.setImageResource(R.drawable.emo_sad)
                    "upset" -> recom.setImageResource(R.drawable.emo_upset)
                }
                i += 1
            }
            if ((i - 1) % 3 == 1) {
                var recom : ImageView = findViewById<ImageView>(resources.getIdentifier("recom" + i, "id", "kr.ac.cau.easyconnect"))
                var recom2 : ImageView = findViewById<ImageView>(resources.getIdentifier("recom" + (i + 1), "id", "kr.ac.cau.easyconnect"))
                recom.visibility = View.INVISIBLE
                recom.setImageResource(R.drawable.emo_upset)
                recom2.visibility = View.INVISIBLE
                recom2.setImageResource(R.drawable.emo_upset)
            } else if ((i - 1) % 3 == 2) {
                var recom : ImageView = findViewById<ImageView>(resources.getIdentifier("recom" + i, "id", "kr.ac.cau.easyconnect"))
                recom.visibility = View.INVISIBLE
                recom.setImageResource(R.drawable.emo_upset)
            }
        }
    }

    override fun onBackPressed() {
        // 이모티콘 켜있을 때 이모티콘 없애고 키보드로 돌아가기
        if (emoticonContainer!!.visibility == View.VISIBLE) {
            emoticonContainer!!.visibility = View.GONE
            showKeyboard()
        }
        else {
            super.onBackPressed()

        }

    }
    // 키보드 없애기
    @SuppressLint("ServiceCast")
    fun hideKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(replyContent!!.windowToken, 0)

    }
    // 이모티콘 없애기
    fun hideEmoticon() {
        emoticonContainer!!.visibility = View.GONE
    }

    fun showKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED,
        InputMethodManager.HIDE_IMPLICIT_ONLY)
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        return true
    }

    inner class ReplyAdapter() : RecyclerView.Adapter<ReplyAdapter.ReplyViewHolder>() {
        var arrayReplyDTO : ArrayList<ReplyDTO> = arrayListOf()

        init {
            db!!.collection("post/" + id + "/reply").addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                arrayReplyDTO.clear()
                for (snapshot in querySnapshot!!.documents) {
                    var reply = snapshot.toObject(ReplyDTO::class.java)
                    arrayReplyDTO!!.add(reply!!)
                }
                notifyDataSetChanged()
            }
        }

        override fun getItemCount(): Int {
            return arrayReplyDTO.size
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): ReplyAdapter.ReplyViewHolder {
            val inflatedView = LayoutInflater.from(parent.context)
                .inflate(R.layout.reply_list, parent, false)
            return ReplyViewHolder(inflatedView)
        }

        override fun onBindViewHolder(holder: ReplyAdapter.ReplyViewHolder, position: Int) {
            val replyDTO = arrayReplyDTO[position]
            holder.apply {
                bind(replyDTO)
            }
        }

        inner class ReplyViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            var view : View = v
            var name: TextView = view.findViewById(R.id.name)
            var registration: TextView = view.findViewById(R.id.registration)
            var content: TextView = view.findViewById(R.id.content)
            var replyDelete : Button = view.findViewById(R.id.replyDelete)
            var replyUpdate : Button = view.findViewById(R.id.replyUpdate)
            var emoticon : ImageView = view.findViewById(R.id.emoticon)
            var replyLayout : View = view.findViewById(R.id.replyLayout)
//            var emoticonContainer : View = parent.findViewById(R.id.emoticonContainer)



            fun bind(reply: ReplyDTO) {
                content.setText(reply.content)
                // 본인이 쓴 댓글 아니면 버튼 안보이게 함

                if (firebaseAuth!!.currentUser.email == reply.name) {
                    replyDelete.visibility = View.VISIBLE
                    replyUpdate.visibility = View.VISIBLE
                }
                // 이모티콘 있을 때
                if (reply.emoticon != null) {
                    emoticon.visibility = View.VISIBLE
                    when (reply.emoticon) {
                        "amazing" -> emoticon.setImageResource(R.drawable.emo_amazing)
                        "angry" -> emoticon.setImageResource(R.drawable.emo_angry)
                        "cry" -> emoticon.setImageResource(R.drawable.emo_cry)
                        "hello" -> emoticon.setImageResource(R.drawable.emo_hello)
                        "good" -> emoticon.setImageResource(R.drawable.emo_good)
                        "great" -> emoticon.setImageResource(R.drawable.emo_great)
                        "curiosity" -> emoticon.setImageResource(R.drawable.emo_curiosity)
                        "hi" -> emoticon.setImageResource(R.drawable.emo_hi)
                        "love" -> emoticon.setImageResource(R.drawable.emo_love)
                        "nice" -> emoticon.setImageResource(R.drawable.emo_nice)
                        "sad" -> emoticon.setImageResource(R.drawable.emo_sad)
                        "upset" -> emoticon.setImageResource(R.drawable.emo_upset)
                    }
                    // 이모티콘 사용시 수정버튼 비활성화
                    replyUpdate.isEnabled = false
                }


                registration.setText(SimpleDateFormat("MM월 dd일 kk:mm").format(reply.registered))
                // 유저 이름 받기위해 DB 탐색
                db!!.collection("user_information").whereEqualTo("email", reply.name).get().addOnCompleteListener{
                    if (it.isSuccessful) {
                        storage = FirebaseStorage.getInstance()
                        val storageReference = storage!!.reference

                        for(dc in it.result!!.documents) {
                            userData = dc.toObject(UserDTO::class.java)
                            break
                        }
                        if (userData != null) {
                            name.setText(userData!!.name)
                        }

                    }
                }
                // 이모티콘이랑 키보드 없애기
               replyLayout.setOnClickListener {
                    val intent = Intent(view.context, ReplyActivity::class.java).apply {
                        hideEmoticon()
                        hideKeyboard()
                    }
                }


                // 댓글 삭제 버튼 클릭
                replyDelete.setOnClickListener {
                    val intent = Intent(view.context, ReplyActivity::class.java).apply {    // fragment 에서 context 값을 받기 위한 작업
                        val dialog = AlertDialog.Builder(view.context)
                        dialog.setTitle("삭제하시겠습니까?")
                        // 확인시 삭제 처리 할 리스너
                        var listener = DialogInterface.OnClickListener { dialog, i ->
                            db!!.collection("post/" + id + "/reply").document(reply.registered.toString()).delete()
                            Toast.makeText(view.context, "삭제완료", Toast.LENGTH_SHORT).show()
                        }
                        dialog.setPositiveButton("확인", listener)
                        dialog.setNegativeButton("취소", null)
                        dialog.show()
                    }
                }
                // 댓글 수정 버튼 클릭
                replyUpdate.setOnClickListener {
                    val intent = Intent(view.context, ReplyUpdate::class.java)
                    intent.putExtra("path", "post/" + id + "/reply")
                    intent.putExtra("id", reply.modified)
                    startActivity(intent)
                }
            }

        }

    }
}
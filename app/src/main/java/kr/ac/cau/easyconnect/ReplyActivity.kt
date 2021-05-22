package kr.ac.cau.easyconnect

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
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
        val replyContent : EditText = findViewById(R.id.reply_content)

        //어댑터 연결 해야 나옴
        replyRecyclerview.adapter = ReplyAdapter()
        replyRecyclerview.layoutManager = LinearLayoutManager(this)
        //댓글 등록시
        findViewById<Button>(R.id.reply_button).setOnClickListener {

            var inputReply = replyContent.text.trim().toString()
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
                    } else {
                        Toast.makeText(this, "failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }

        }
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
            fun bind(reply: ReplyDTO) {
                content.setText(reply.content)
                // 본인이 쓴 댓글 아니면 안보이게 함
                if (firebaseAuth!!.currentUser.email != reply.name) {
                    replyDelete.visibility = View.GONE
                    replyUpdate.visibility = View.GONE
                }
                /*
                if (reply.emoticon == null) {
                    emoticon.visibility = View.GONE
                }
                */

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
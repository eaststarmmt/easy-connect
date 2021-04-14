package kr.ac.cau.easyconnect

import android.app.SearchManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Page_find_friends : AppCompatActivity() {

    private var friend_adapter: FriendAdapter? = null

    var userList = arrayListOf<FriendItem_recyclerView>(
        FriendItem_recyclerView("이형석", "sleep_cat.jpg"),
        FriendItem_recyclerView("이형석2", "sample.jpg"),
        FriendItem_recyclerView("최명원", "wow.jpg"),
        FriendItem_recyclerView("이형석4", "sample.jpg"),
        FriendItem_recyclerView("김동규", "wow.jpg"),
        FriendItem_recyclerView("이형석6", "sample.jpg"),
        FriendItem_recyclerView("이형석7", "wow.jpg")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page_find_friends)

        val recycler_view : RecyclerView = findViewById(R.id.recycler_friend)
        val button_searchFriend : Button = findViewById(R.id.bt_searchFriend)
        val editText_searchFriend : EditText = findViewById(R.id.edit_searchFriend)

        val mAdapter = FriendAdapter(this, userList)
        recycler_view.adapter = mAdapter

        val layout = LinearLayoutManager(this)
        recycler_view.layoutManager = layout
        recycler_view.setHasFixedSize(true)

        button_searchFriend.setOnClickListener({
            (friend_adapter as FriendAdapter).search(editText_searchFriend.text.toString(), userList)
        })

    }



}
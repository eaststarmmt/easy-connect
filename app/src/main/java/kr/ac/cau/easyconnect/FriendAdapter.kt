package kr.ac.cau.easyconnect

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView

class FriendAdapter(private val context: Context, private val dataList: ArrayList<FriendItem_recyclerView>):
    RecyclerView.Adapter<FriendAdapter.SearchViewHolder>() {

    private var dataSearchList: ArrayList<FriendItem_recyclerView>? = dataList
    var mPosition = 0


    fun getPosition(): Int{
        return mPosition
    }
    fun setPosition(position: Int){
        mPosition = position
    }

    fun addItem(data: FriendItem_recyclerView){
        dataList.add(data)
        // 갱신
//        noifyDataSetChanged()
    }

    inner class SearchViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        var friend_photo = itemView.findViewById<ImageView>(R.id.img_friend)
        var friend_name = itemView.findViewById<Button>(R.id.bt_friendName)

        fun bind(frienditemRecyclerview: FriendItem_recyclerView, context: Context){
            if(frienditemRecyclerview.photo != ""){
                val resourceId = context.resources.getIdentifier(frienditemRecyclerview.photo, "drawable", context.packageName)

                if(resourceId > 0){
                    friend_photo.setImageResource(resourceId)
                }
                else{
                    friend_photo.setImageResource(R.mipmap.ic_launcher_round)
                }
            }
            else{
                friend_photo.setImageResource(R.mipmap.ic_launcher_round)
            }

            friend_name.text = frienditemRecyclerview.name
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.friend_item_list, parent, false)
        return SearchViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        var viewHolder: SearchViewHolder = holder

        viewHolder.friend_name.text = dataList[position].name
//        val item = dataList[position]

//        val listener = View.OnClickListener { it ->
//            Toast.makeText(it.context, "Clicked", Toast.LENGTH_SHORT).show()
//        }

//        holder.apply{ 이거
//            holder.bind(item, context)  이거
//              itemView.tag = item
//        } 이거

        //////////////////////////////////
        holder.bind(dataList[position], context)
//        holder.itemView.setOnClickListener{ view ->
//            setPosition(position)
//        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
//
//    override fun getFilter(): Filter {
//        return object: Filter() {
//            override fun performFiltering(charSeq: CharSequence): FilterResults {
//                val charString = charSeq.toString()
//                if(charString.isEmpty()){
//                    dataSearchList = dataList
//                }
//                else{
//                    val filteredList = ArrayList<FriendItem_recyclerView>()
//                    for (row in dataList){
//                        if(row.phone.toLowerCase().contains(charString.toLowerCase()))
//                            filteredList.add(row)
//                    }
//                    dataSearchList = filteredList
//                }
//                val filterResults = FilterResults()
//                filterResults.values = dataSearchList
//                return filterResults
//            }
//
//            override fun publishResults(charSeq: CharSequence?, filterResults: FilterResults) {
//                dataSearchList = filterResults.values as ArrayList<FriendItem_recyclerView>
//                notifyDataSetChanged()
//            }
//        }
//    }
//
//    interface ItemOnClickListener {
//        fun onItemClicked(item : FriendItem_recyclerView)
//    }

    fun search(searchName : String, userlist: ArrayList<FriendItem_recyclerView>){
        for (snapshot in userlist) {
            if (snapshot.name.contains(searchName)) {
                var item : FriendItem_recyclerView = snapshot
                dataSearchList!!.add(item)
            }
        }
        notifyDataSetChanged()
    }
}
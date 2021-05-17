package kr.ac.cau.easyconnect

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import com.myhome.siviewpager.SIPagerAdapter

class DetailViewAdapter(context: Context): SIPagerAdapter() {
    val context = context
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(context).inflate(R.layout.detail_pager_item, container, false)

        return view
    }
}
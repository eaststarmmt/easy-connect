package kr.ac.cau.easyconnect

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import kr.ac.cau.easyconnect.databinding.ActivityDetailBinding

class ViewPagerAdaper(fm: FragmentManager): FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    var PAGENUMBER = 3

    override fun getCount(): Int {
        return PAGENUMBER
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> DetailFragment.newInstance(R.raw.img00, "test 00")
            1 -> DetailFragment.newInstance(R.raw.img01, "test 01")
            else -> DetailFragment.newInstance(R.raw.img00, "test 02")

        }
    }
}
package kr.ac.cau.easyconnect

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import androidx.annotation.LayoutRes

class AutoCompleteAdapter(private val c: Context, @LayoutRes private val layoutResource: Int, private val movies: MutableList<WriteActivity.Movie?>) :
ArrayAdapter<WriteActivity.Movie>(c, layoutResource, movies) {



    var filteredMovies: MutableList<WriteActivity.Movie?> = mutableListOf()

    var content : AutoCompleteAdapter? = null
    override fun getCount(): Int = filteredMovies.size

    override fun getItem(position: Int): WriteActivity.Movie =
        filteredMovies[position]!!

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(c).inflate(layoutResource, parent, false)
        val hashTag = view.findViewById<TextView>(R.id.hashTag)
        val count = view.findViewById<TextView>(R.id.count)
        hashTag.text = filteredMovies[position]!!.name
        count.text = filteredMovies[position]!!.year.toString()

        return view
    }

    override fun getFilter(): Filter {
        return object: Filter() {
            override fun publishResults(charSequence: CharSequence?, filterResults: FilterResults?) {
               filteredMovies = filterResults!!.values as MutableList<WriteActivity.Movie?>
                notifyDataSetChanged()
            }

            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                val queryString = charSequence?.toString()

                val filterResults = FilterResults()
                filterResults.values = if (queryString == null || queryString.isEmpty()) {
                    movies.filter {
                        it!!.name.contains(" ") || it.year.toString().contains(" ")
                    }
                } else {
                    movies.filter {
                        it!!.name.contains(queryString) || it.year.toString().contains(queryString)
                    }
                }
                return filterResults

            }
        }
    }

}
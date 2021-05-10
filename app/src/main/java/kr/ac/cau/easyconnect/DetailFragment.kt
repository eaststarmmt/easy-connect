package kr.ac.cau.easyconnect

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kr.ac.cau.easyconnect.databinding.DetailPagerItemBinding

class DetailFragment : Fragment() {
    private var image: Int? = null
    private var text: String? = null
    lateinit var binding: DetailPagerItemBinding

    var storage: FirebaseStorage = FirebaseStorage.getInstance()
    var firebaseAuth: FirebaseAuth? = null
    var db: FirebaseFirestore? = null
    val storageReference = storage!!.reference

    val bundle : Bundle? = arguments
    var imgFileName : String? = bundle!!.getString("data")



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        storage = FirebaseStorage.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        arguments?.let {
            image = it.getInt("image", 0)
            text = it.getString("text", "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DetailPagerItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        storageReference.child("post/" + imgFileName).downloadUrl.addOnSuccessListener {
            Glide.with(this).load(it).into(binding.imageView)
        }
//        binding.imageView.setImageResource(image!!)

    }

    companion object {
        fun newInstance(image: Int, text: String) =
            DetailFragment().apply {
                arguments = Bundle().apply {
                    putInt("image", image)
                    putString("text", text)
                }
            }
    }
}
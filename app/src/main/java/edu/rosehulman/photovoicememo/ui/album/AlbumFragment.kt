package edu.rosehulman.photovoicememo.ui.album

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.rosehulman.photovoicememo.R
import edu.rosehulman.photovoicememo.databinding.FragmentAlbumBinding
import edu.rosehulman.photovoicememo.model.Constants
import edu.rosehulman.photovoicememo.model.PhotoVoice


/**
 * Fragment that demonstrates a responsive layout pattern where the format of the content
 * transforms depending on the size of the screen. Specifically this Fragment shows items in
 * the [RecyclerView] using LinearLayoutManager in a small screen
 * and shows items using GridLayoutManager in a large screen.
 */
class AlbumFragment : Fragment() {

    private lateinit var model: AlbumViewModel
    private lateinit var binding: FragmentAlbumBinding


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        model = ViewModelProvider(this).get(AlbumViewModel::class.java)
        binding = FragmentAlbumBinding.inflate(inflater, container, false)
//        val root: View = binding.root

        val recyclerView = binding.recyclerViewAlbum
        val adapter = AlbumAdapter(this)
        adapter.addnew()
        recyclerView?.adapter = adapter
        recyclerView?.layoutManager =  GridLayoutManager(requireContext(),2)
        model.texts.observe(viewLifecycleOwner, {
            adapter.submitList(it)
        })
        return binding.root
    }



    class AlbumAdapter(val fragment: AlbumFragment) :
        ListAdapter<String, AlbumViewHolder>(object : DiffUtil.ItemCallback<String>() {

            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean =
                oldItem == newItem

            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean =
                oldItem == newItem
        }) {

        fun addnew(){
            Firebase.firestore.collection(Constants.COLLECTION_PATH).add(PhotoVoice("xxx","yyy"))
            Log.d(Constants.TAG,"addtoFirebase")

        }

        private val drawables = listOf(
            R.drawable.avatar_1,
            R.drawable.avatar_2,
            R.drawable.avatar_3,
            R.drawable.avatar_4,
            R.drawable.avatar_5,
            R.drawable.avatar_6,
            R.drawable.avatar_7,
            R.drawable.avatar_8,
            R.drawable.avatar_9,
            R.drawable.avatar_10,
            R.drawable.avatar_11,
            R.drawable.avatar_12,
            R.drawable.avatar_13,
            R.drawable.avatar_14,
            R.drawable.avatar_15,
            R.drawable.avatar_16,
        )

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
            //val view = AlbumItemTransformBinding.inflate(LayoutInflater.from(parent.context))
            val view = LayoutInflater.from(parent.context).inflate(R.layout.album_item_transform,parent,false)
            return AlbumViewHolder(view,fragment)
        }

        override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
            holder.textView.text = getItem(position)

            holder.imageView.setImageDrawable(
                ResourcesCompat.getDrawable(holder.imageView.resources, drawables[position], null)
            )
        }

        }
    }

//     class AlbumViewHolder(binding: AlbumItemTransformBinding) : RecyclerView.ViewHolder(binding.root) {
class AlbumViewHolder(itemView: View, fragment: AlbumFragment) : RecyclerView.ViewHolder(itemView) {

    val imageView: ImageView =itemView.findViewById(R.id.thumbnail_view)
    val textView: TextView = itemView.findViewById(R.id.caption_detail)

    init {
             itemView.setOnClickListener{
                 fragment.findNavController().navigate(R.id.nav_photo,
                     null,
                     navOptions{
                         anim{
                             enter = android.R.anim.slide_in_left
                             exit = android.R.anim.slide_out_right
                         }
                     }
                 )
             }

    }





}
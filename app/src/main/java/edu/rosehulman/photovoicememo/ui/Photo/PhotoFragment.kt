package edu.rosehulman.photovoicememo.ui.Photo

import android.graphics.Color
import android.os.Bundle
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
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import edu.rosehulman.photovoicememo.R
import edu.rosehulman.photovoicememo.databinding.FragmentPhotoBinding
import edu.rosehulman.photovoicememo.model.PhotoVoice
import edu.rosehulman.photovoicememo.model.PhotoVoiceViewModel


class PhotoFragment : Fragment() {

    private lateinit var model: PhotoVoiceViewModel
    private lateinit var binding: FragmentPhotoBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        model = ViewModelProvider(this).get(PhotoVoiceViewModel::class.java)
        binding = FragmentPhotoBinding.inflate(inflater, container, false)
        val recyclerView = binding.recyclerviewPhoto
        val adapter = PhotoAdapter(this)
        recyclerView.adapter = adapter
        adapter.addListener(fragmentName)
        recyclerView.setHasFixedSize(true)
        recyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))


//        model.texts.observe(viewLifecycleOwner, {
//            adapter.submitList(it)
//        })
        return binding.root
    }

    companion object{
        const val fragmentName = "PhotoFragment"
    }
    class PhotoAdapter(val fragment: PhotoFragment) : RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {//???
            val model = ViewModelProvider(fragment.requireActivity()).get(PhotoVoiceViewModel::class.java)

//            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean =
//                oldItem == newItem
//
//            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean =
//                oldItem == newItem
//        }) {
//
//        private val drawables = listOf(
//            R.drawable.avatar_1,
//            R.drawable.avatar_2,
//            R.drawable.avatar_3,
//            R.drawable.avatar_4,
//            R.drawable.avatar_5,
//            R.drawable.avatar_6,
//            R.drawable.avatar_7,
//            R.drawable.avatar_8,
//            R.drawable.avatar_9,
//            R.drawable.avatar_10,
//            R.drawable.avatar_11,
//            R.drawable.avatar_12,
//            R.drawable.avatar_13,
//            R.drawable.avatar_14,
//            R.drawable.avatar_15,
//            R.drawable.avatar_16,
//        )

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {

            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transform,parent,false)
            return PhotoViewHolder(view)
        }

        override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
            holder.bind(model.getPhotoVoiceAt(position))
//            holder.textView.text = getItem(position)
//            holder.imageView.setImageDrawable(
//                ResourcesCompat.getDrawable(holder.imageView.resources, drawables[position], null)
//            )
        }
        fun addListener(fragmentName: String){
            model.addListener(fragmentName){
                notifyDataSetChanged()
            }
        }

        fun removeListener(fragmentName: String) {
            model.removeListener(fragmentName)
        }

        override fun getItemCount() = model.size()

    inner class PhotoViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView) {

        val imageView: ImageView = itemView.findViewById(R.id.image_view_item_transform)
        val textView: TextView = itemView.findViewById(R.id.text_view_item_transform)

        init {
            itemView.setOnClickListener {
                model.updatePos(absoluteAdapterPosition)
                fragment.findNavController().navigate(R.id.nav_photo_detail,
                    null,
                    navOptions {
                        anim {
                            enter = android.R.anim.slide_in_left
                            exit = android.R.anim.slide_out_right
                        }
                    }
                )
            }

        }
        fun bind(photoVoice: PhotoVoice){
            textView.text = photoVoice.created.toString()
            imageView.load(photoVoice.photo){
                crossfade(true)
                transformations(CircleCropTransformation())
            }
        }

    }
    }
}




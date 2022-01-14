package edu.rosehulman.photovoicememo.ui.Photo

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
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.rosehulman.photovoicememo.R
import edu.rosehulman.photovoicememo.databinding.FragmentPhotoBinding


class PhotoFragment : Fragment() {

    private lateinit var model: PhotoViewModel
    private lateinit var binding: FragmentPhotoBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        model = ViewModelProvider(this).get(PhotoViewModel::class.java)
        binding = FragmentPhotoBinding.inflate(inflater, container, false)

        val recyclerView = binding.recyclerviewPhoto
        val adapter = PhotoAdapter(this)
        recyclerView.adapter = adapter
        model.texts.observe(viewLifecycleOwner, {
            adapter.submitList(it)
        })
        return binding.root
    }


    class PhotoAdapter(val fragment: PhotoFragment) :
        ListAdapter<String, PhotoViewHolder>(object : DiffUtil.ItemCallback<String>() {

            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean =
                oldItem == newItem

            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean =
                oldItem == newItem
        }) {

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

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {

            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transform,parent,false)
            return PhotoViewHolder(view, fragment)
        }

        override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
            holder.textView.text = getItem(position)
            holder.imageView.setImageDrawable(
                ResourcesCompat.getDrawable(holder.imageView.resources, drawables[position], null)
            )
        }
    }

    class PhotoViewHolder(itemView: View, fragment: PhotoFragment) :RecyclerView.ViewHolder(itemView) {

        val imageView: ImageView = itemView.findViewById(R.id.image_view_item_transform)
        val textView: TextView = itemView.findViewById(R.id.text_view_item_transform)

        init {
            itemView.setOnClickListener {
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
    }

}
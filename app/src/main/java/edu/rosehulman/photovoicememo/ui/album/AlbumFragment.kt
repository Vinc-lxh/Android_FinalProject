package edu.rosehulman.photovoicememo.ui.album

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.rosehulman.photovoicememo.R
import edu.rosehulman.photovoicememo.databinding.FragmentAlbumBinding
import edu.rosehulman.photovoicememo.databinding.AlbumItemTransformBinding


/**
 * Fragment that demonstrates a responsive layout pattern where the format of the content
 * transforms depending on the size of the screen. Specifically this Fragment shows items in
 * the [RecyclerView] using LinearLayoutManager in a small screen
 * and shows items using GridLayoutManager in a large screen.
 */
class AlbumFragment : Fragment() {

    private lateinit var model: AlbumViewModel
    private var _binding: FragmentAlbumBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        model = ViewModelProvider(this).get(AlbumViewModel::class.java)
        _binding = FragmentAlbumBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val recyclerView = binding.recyclerViewAlbum
        val adapter = AlbumAdapter()
        recyclerView?.adapter = adapter
        recyclerView?.layoutManager =  GridLayoutManager(requireContext(),2)
        model.texts.observe(viewLifecycleOwner, {
            adapter.submitList(it)
        })
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class AlbumAdapter :
        ListAdapter<String, TransformViewHolder>(object : DiffUtil.ItemCallback<String>() {

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

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransformViewHolder {
            val binding = AlbumItemTransformBinding.inflate(LayoutInflater.from(parent.context))
            return TransformViewHolder(binding)
        }

        override fun onBindViewHolder(holder: TransformViewHolder, position: Int) {
            holder.textView.text = getItem(position)
            holder.imageView.setImageDrawable(
                ResourcesCompat.getDrawable(holder.imageView.resources, drawables[position], null)
            )
        }
    }

    class TransformViewHolder(binding: AlbumItemTransformBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val imageView: ImageView = binding.thumbnailView
        val textView: TextView = binding.captionDetail
    }
}
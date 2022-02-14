package edu.rosehulman.photovoicememo.ui.album

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.media.Image
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import coil.transform.RoundedCornersTransformation
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import edu.rosehulman.photovoicememo.R
import edu.rosehulman.photovoicememo.databinding.FragmentAlbumBinding
import edu.rosehulman.photovoicememo.model.Album
import edu.rosehulman.photovoicememo.model.Album.Companion.defaultAlbumPage
import edu.rosehulman.photovoicememo.model.Constants
import edu.rosehulman.photovoicememo.model.PhotoVoiceViewModel


/**
 * Fragment that demonstrates a responsive layout pattern where the format of the content
 * transforms depending on the size of the screen. Specifically this Fragment shows items in
 * the [RecyclerView] using LinearLayoutManager in a small screen
 * and shows items using GridLayoutManager in a large screen.
 */
class AlbumFragment : Fragment() {

    private lateinit var model: PhotoVoiceViewModel
    private lateinit var binding: FragmentAlbumBinding
    private lateinit var adapter:AlbumAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        model = ViewModelProvider(this).get(PhotoVoiceViewModel::class.java)
        binding = FragmentAlbumBinding.inflate(inflater, container, false)
        val recyclerView = binding.recyclerViewAlbum
        adapter = AlbumAdapter(this)
        adapter.addAlbumListener(fragmentName)
        //adapter.addPhotoListner(fragmentName)
        recyclerView?.adapter = adapter
        recyclerView?.setHasFixedSize(true)
        recyclerView?.layoutManager =  GridLayoutManager(requireContext(),2)
        binding.addAlbum?.setOnClickListener {
            createTextDialog(requireContext()){
                adapter.addAlbum(Album(name = it, url = defaultAlbumPage))
           }
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter.removeListener(fragmentName)
    }

    companion object{
        const val fragmentName = "AlbumFragment"
    }


     fun createTextDialog(context: Context,observer: (String) -> Unit) {
            val textInputLayout = TextInputLayout(context)
            textInputLayout.setPadding(
                resources.getDimensionPixelOffset(R.dimen.dp_19),
                0,
                resources.getDimensionPixelOffset(R.dimen.dp_19),
                0
            )
            val input = EditText(context)
            textInputLayout.addView(input)

            val alert = AlertDialog.Builder(context)
                .setTitle("Enter your album name")
                .setView(textInputLayout)
                .setPositiveButton("Submit") { dialog, _ ->
                    val txt = input.text.toString()
                    Log.d(Constants.TAG,"txt is "+txt)
                    observer(txt)
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }.create()

            alert.show()
        }

    class AlbumAdapter(val fragment: AlbumFragment): RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder>()  {
        val model = ViewModelProvider(fragment.requireActivity()).get(PhotoVoiceViewModel::class.java)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.album_item_transform,parent,false)
            return AlbumViewHolder(view)
        }

        override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
            holder.bind(model.getAlbumAt(position))
        }
        fun addAlbumListener(fragmentName: String) {
            model.addAlbumListener(fragmentName){
                notifyDataSetChanged()
            }
        }
        fun addPhotoListner(fragmentName: String) {
            model.addListener(fragmentName){
                notifyDataSetChanged()
            }
        }
        fun removeListener(fragmentName: String) {
            model.removeListener(fragmentName)
        }
        fun addAlbum(album: Album) {
            model.addAlbum(album)
        }
    inner class AlbumViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val imageView: ImageView =itemView.findViewById(R.id.thumbnail_view)
        val textView: TextView = itemView.findViewById(R.id.caption_detail)

        init {
            itemView.setOnClickListener{
                model.updateAlbumPos(absoluteAdapterPosition)
                Log.d(Constants.TAG,"currently at album $absoluteAdapterPosition")
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
            itemView.setOnLongClickListener {
                val builder = androidx.appcompat.app.AlertDialog.Builder(itemView.context)
                builder.setTitle("Delete Album")
                builder.setMessage("Do you want to delete the album ${model.getCurrentAlbum().name} and all corresponding photo voice?")
                builder.setPositiveButton("Yes") { _, _ ->
                    model.updateAlbumPos(absoluteAdapterPosition)
                    model.removeCurrentAlbum()
                    notifyDataSetChanged()
                }

                builder.setNegativeButton("No") { dia, _ ->
                    dia.dismiss()
                }
                builder.create().show()
              true

            }

        }
        fun bind(album: Album){
            if(album.name.length>5){
                textView.text = album.name.substring(0,5)+"..."
            }
            else{
                textView.text = album.name
            }

            imageView.load(album.url){
                crossfade(true)
                transformations(RoundedCornersTransformation())
            }
        }
    }

        override fun getItemCount() = model.Albumsize()


    }
}


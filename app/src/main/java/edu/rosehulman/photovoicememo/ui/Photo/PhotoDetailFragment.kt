package edu.rosehulman.photovoicememo.ui.Photo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import coil.load
import coil.transform.CircleCropTransformation
import edu.rosehulman.photovoicememo.R
import edu.rosehulman.photovoicememo.databinding.FragmentPhotoDetailBinding

class PhotoDetailFragment : Fragment() {
   private  lateinit var model: PhotoViewModel
   private lateinit var binding: FragmentPhotoDetailBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPhotoDetailBinding.inflate(inflater,container,false)

        model = ViewModelProvider(requireActivity()).get(PhotoViewModel::class.java)

//        updateView()
        // Inflate the layout for this fragment
//        binding.photoDetailView.load(R.drawable.avatar_11) {
//            crossfade(true)
//            transformations(CircleCropTransformation())
//        }
        return binding.root
    }

//    fun updateView(){
//        binding.captionDetail.text =
//
//        binding.imageView.load(model.toString()) {
//            crossfade(true)
//            transformations(CircleCropTransformation())
//        }


    }


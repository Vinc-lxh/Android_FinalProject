package edu.rosehulman.photovoicememo.ui.Photo

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import coil.load
import coil.transform.CircleCropTransformation
import edu.rosehulman.photovoicememo.R
import edu.rosehulman.photovoicememo.databinding.FragmentPhotoDetailBinding
import edu.rosehulman.photovoicememo.model.Constants
import edu.rosehulman.photovoicememo.model.PhotoVoice
import edu.rosehulman.photovoicememo.model.PhotoVoiceViewModel
import java.io.IOException

class PhotoDetailFragment : Fragment() {
   private var player: MediaPlayer? = null
   private  lateinit var model: PhotoVoiceViewModel
   private lateinit var binding: FragmentPhotoDetailBinding
   private var playing = true
    private lateinit var photoVoice: PhotoVoice
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPhotoDetailBinding.inflate(inflater,container,false)
        model = ViewModelProvider(requireActivity()).get(PhotoVoiceViewModel::class.java)
        photoVoice = model.getCurrentPhoto()
        updateView()
        setupButton()
        return binding.root
    }

    private fun setupButton() {

        binding.playerPlayBtn.setOnClickListener {


        }

    }

    fun updateView() {


        binding.captionDetail.text = photoVoice.created.toString()
        binding.photoDetailView.load(photoVoice.photo) {
                crossfade(true)
                transformations(CircleCropTransformation())
        }
    }



}


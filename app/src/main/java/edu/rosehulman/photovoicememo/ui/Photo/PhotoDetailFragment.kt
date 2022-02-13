package edu.rosehulman.photovoicememo.ui.Photo

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import coil.load
import coil.transform.RoundedCornersTransformation
import edu.rosehulman.photovoicememo.R
import edu.rosehulman.photovoicememo.databinding.FragmentPhotoDetailBinding
import edu.rosehulman.photovoicememo.model.Constants
import edu.rosehulman.photovoicememo.model.PhotoVoice
import edu.rosehulman.photovoicememo.model.PhotoVoiceViewModel

import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.time.Duration.Companion.days


class PhotoDetailFragment : Fragment() {

    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false

    //UI Elements
    private lateinit var playBtn: ImageButton

    private lateinit var playerFilename: TextView

    private lateinit var playerSeekbar: SeekBar
    private lateinit var seekbarHandler: Handler
    private lateinit var updateSeekbar: Runnable


   private  lateinit var model: PhotoVoiceViewModel
   private lateinit var binding: FragmentPhotoDetailBinding
    private lateinit var photoVoice: PhotoVoice

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPhotoDetailBinding.inflate(inflater,container,false)
        model = ViewModelProvider(requireActivity()).get(PhotoVoiceViewModel::class.java)
        photoVoice = model.getCurrentPhoto()
        updateView()

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        playAudio()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playBtn = view.findViewById(R.id.detail_player_play_btn)

        playerFilename = view.findViewById(R.id.detail_caption_detail)
        playerSeekbar = view.findViewById(R.id.detail_player_seekbar)

        playBtn.setOnClickListener {
            if (isPlaying) {
                pauseAudio()
            } else {
                if (mediaPlayer != null) {
            Log.d(Constants.TAG,"$isPlaying")
                    resumeAudio()
                }
            }
        }
        playerSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {
                pauseAudio()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                val progress = seekBar.progress
                mediaPlayer!!.seekTo(progress)
                resumeAudio()
            }
        })

    }

    private fun pauseAudio() {
        mediaPlayer!!.pause()
        playBtn.setImageDrawable(requireActivity().resources.getDrawable(R.drawable.ic_baseline_play_arrow_24, null))
        isPlaying = false
        seekbarHandler.removeCallbacks(updateSeekbar)
    }

    private fun resumeAudio() {
        mediaPlayer!!.start()
        playBtn.setImageDrawable(
            requireActivity().resources.getDrawable(
                R.drawable.ic_baseline_pause_24,
                null
            )
        )
        isPlaying = true
        updateRunnable()
        seekbarHandler.postDelayed(updateSeekbar, 0)
    }

    private fun stopAudio() {
        //Stop The Audio
        playBtn.setImageDrawable(requireActivity().resources.getDrawable(R.drawable.ic_baseline_play_arrow_24, null))

        isPlaying = false
        mediaPlayer!!.stop()
        seekbarHandler.removeCallbacks(updateSeekbar)
    }


    private fun playAudio() {
        mediaPlayer = MediaPlayer()
        try {
            mediaPlayer!!.setDataSource(model.getCurrentPhoto().voice)
            Log.d(Constants.TAG, "${model.getCurrentPhoto().voice}")
//            mediaPlayer!!.setDataSource(fileToPlay.absolutePath)
            mediaPlayer!!.prepare()
            mediaPlayer!!.start()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        playBtn.setImageDrawable(
            requireActivity().resources.getDrawable(
                R.drawable.ic_baseline_pause_24,
                null
            )
        )
        val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.US)
        val dateStr: String = sdf.format(photoVoice.created?.toDate()?.time)
        playerFilename.text = dateStr

        //Play the audio
        isPlaying = true
        mediaPlayer!!.setOnCompletionListener(MediaPlayer.OnCompletionListener {
            stopAudio()

        })
        playerSeekbar.max = mediaPlayer!!.getDuration()
        seekbarHandler = Handler(Looper.getMainLooper())
        updateRunnable()
        seekbarHandler.postDelayed(updateSeekbar, 0)
    }

    private fun updateRunnable() {
        updateSeekbar = object : Runnable {
            override fun run() {
                playerSeekbar.progress = mediaPlayer!!.currentPosition
                seekbarHandler.postDelayed(this, 500)
            }
        }
    }
    override fun onStop() {
        super.onStop()
        if (isPlaying) {
            stopAudio()
        }
    }



    fun updateView() {
        val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.US)
        val dateStr: String = sdf.format(photoVoice.created?.toDate()?.time)
        binding.detailCaptionDetail.setText(dateStr)
        binding.photoDetailView.load(photoVoice.photo) {
                crossfade(true)
                transformations(RoundedCornersTransformation())
        }
    }



}


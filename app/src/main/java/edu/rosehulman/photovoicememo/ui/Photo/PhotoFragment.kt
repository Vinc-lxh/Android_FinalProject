package edu.rosehulman.photovoicememo.ui.Photo

import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat.invalidateOptionsMenu
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.button.MaterialButton
import com.leinardi.android.speeddial.SpeedDialView
import edu.rosehulman.photovoicememo.BuildConfig
import edu.rosehulman.photovoicememo.R
import edu.rosehulman.photovoicememo.databinding.FragmentPhotoBinding
import edu.rosehulman.photovoicememo.model.Constants
import edu.rosehulman.photovoicememo.model.PhotoVoice
import edu.rosehulman.photovoicememo.model.PhotoVoiceViewModel
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class PhotoFragment : Fragment() {

    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>
    private lateinit var playerSheet: ConstraintLayout
    private var latestTmpUri: Uri? = null
    //UI Elements
    private lateinit var playBtn: ImageButton
    private lateinit var playerHeader: TextView
    private lateinit var playerFilename: TextView

    private lateinit var playerSeekbar: SeekBar
    private lateinit var seekbarHandler: Handler
    private lateinit var updateSeekbar: Runnable

    private lateinit var model: PhotoVoiceViewModel
    private lateinit var binding: FragmentPhotoBinding
    private lateinit var adapter: PhotoAdapter
    private lateinit var recyclerView:RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        model = ViewModelProvider(this).get(PhotoVoiceViewModel::class.java)
        binding = FragmentPhotoBinding.inflate(inflater, container, false)
        recyclerView = binding.recyclerviewPhoto
        adapter = PhotoAdapter(this)
        adapter.addListener(fragmentName)
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )

        settupRecycleViewSwipeForDelete()
        initializeButtons()
        return binding.root
    }

    private fun settupRecycleViewSwipeForDelete() {
        val touchHelperCallback: ItemTouchHelper.SimpleCallback =
            object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                val deleteIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_delete_24)
                val intrinsicWidth = deleteIcon?.intrinsicWidth
                val intrinsicHeight = deleteIcon?.intrinsicHeight
                val background = ColorDrawable()
                val clearPaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }
                val backgroundColor = Color.parseColor("#f44336")
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    Log.d(Constants.TAG,"swipeed")
                    adapter.deletePhoto(viewHolder.absoluteAdapterPosition)
                }

                override fun onChildDraw(
                    c: Canvas,
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    dX: Float,
                    dY: Float,
                    actionState: Int,
                    isCurrentlyActive: Boolean
                ) {

                    val itemView = viewHolder.itemView
                    val itemHeight = itemView.bottom - itemView.top
                    val isCanceled = dX == 0f && !isCurrentlyActive

                    if (isCanceled) {
                        clearCanvas(c, itemView.right + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat())
                        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        return
                    }

                    // Draw the red delete background
                    background.color = backgroundColor
                    background.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
                    background.draw(c)

                    // Calculate position of delete icon
                    val deleteIconTop = itemView.top + (itemHeight - intrinsicHeight!!) / 2
                    val deleteIconMargin = (itemHeight - intrinsicHeight) / 2
                    val deleteIconLeft = itemView.right - deleteIconMargin - intrinsicWidth!!
                    val deleteIconRight = itemView.right - deleteIconMargin
                    val deleteIconBottom = deleteIconTop + intrinsicHeight

                    // Draw the delete icon
                    deleteIcon?.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom)
                    deleteIcon?.draw(c)

                    super.onChildDraw(
                        c,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )
                }

                private fun clearCanvas(c: Canvas?, left: Float, top: Float, right: Float, bottom: Float) {
                    c?.drawRect(left, top, right, bottom, clearPaint)
                }

            }
        val itemTouchHelper = ItemTouchHelper(touchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter.removeListener(fragmentName)
    }

    private fun initializeButtons(){
//        binding.addFab.setOnClickListener {
//            findNavController().navigate(R.id.nav_camera)
//        }

        binding.speedDialFab.inflate(R.menu.menu_speed_dial)
        binding.speedDialFab.setOnActionSelectedListener(
            SpeedDialView.OnActionSelectedListener { actionItem ->
                when (actionItem.id) {
                    R.id.action_local -> {
                        selectImageFromGallery()
                        binding.speedDialFab.close() // To close the Speed Dial with animation

                        return@OnActionSelectedListener true // false will close it without animation
                    }
                    R.id.action_camera -> {
                        takeImage()

                        return@OnActionSelectedListener false
                    }
                }
                false
            })

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        playerSheet = view.findViewById(R.id.player_sheet)
        bottomSheetBehavior = BottomSheetBehavior.from<ConstraintLayout>(playerSheet)
        playBtn = view.findViewById(R.id.player_play_btn)
        playerHeader = view.findViewById(R.id.player_header_title)
        playerFilename = view.findViewById(R.id.player_filename)
        playerSeekbar = view.findViewById(R.id.player_seekbar)

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED)
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                //We cant do anything here for this app
            }
        })
        playBtn.setOnClickListener {
            Log.d(Constants.TAG,"$isPlaying")
            if (isPlaying) {
                pauseAudio()
            } else {
                if (mediaPlayer != null) {
                    resumeAudio()
                }
            }
        }
        playerSeekbar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
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

//    fun onClickListener(file: File, position: Int) {
//        fileToPlay = file
//        if (isPlaying) {
//            stopAudio()
//            playAudio()
//        } else {
//            playAudio()
//        }
//    }



    private fun getTmpFileUri(): Uri {
        val storageDir: File = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val tmpFile = File.createTempFile("JPEG_${timeStamp}_", ".png", storageDir).apply {
            createNewFile()
            deleteOnExit()
        }
        return FileProvider.getUriForFile(
            requireContext(),
            "${BuildConfig.APPLICATION_ID}.provider",
            tmpFile
        )
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Do if the permission is granted
            val toast = Toast.makeText(context,"permission granted",Toast.LENGTH_SHORT)
            toast.show()
        }
        else {
            val toast = Toast.makeText(context,"permission request fail",Toast.LENGTH_SHORT)
            toast.show()
        }
    }

    private val takeImageResult =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
            if (isSuccess) {
                latestTmpUri?.let { uri ->
                    Log.d(Constants.TAG,"uri is $uri")
                    val result = uri.toString()
                    Log.d(Constants.TAG,"uri to String  is $result")
                    // Use the Kotlin extension in the fragment-ktx artifact

                    setFragmentResult("requestKey", bundleOf("bundleKey" to result))
                    findNavController().navigate(R.id.nav_camera)
                }
            }
        }


    private fun takeImage() {
        if(checkCameraPermission()){
            lifecycleScope.launchWhenStarted {
                getTmpFileUri().let { uri ->
                    latestTmpUri = uri
                    takeImageResult.launch(uri)
                }
            }
        }else{
            permissionLauncher.launch(android.Manifest.permission.CAMERA)
        }

    }
    private fun selectImageFromGallery() = selectImageFromGalleryResult.launch("image/*")
    private val selectImageFromGalleryResult =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                Log.d(Constants.TAG,"uri is $uri")
                val result = uri.toString()
                Log.d(Constants.TAG,"uri to String  is $result")
                // Use the Kotlin extension in the fragment-ktx artifact

                setFragmentResult("requestKey", bundleOf("bundleKey" to result))
                findNavController().navigate(R.id.nav_camera)
            }
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
        playerHeader.text = "Stopped"
        isPlaying = false
        mediaPlayer!!.stop()
        seekbarHandler.removeCallbacks(updateSeekbar)
    }


    private fun playAudio() {
        mediaPlayer = MediaPlayer()
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        try {
            mediaPlayer!!.setDataSource(adapter.getCurrentVoice())
            Log.d(Constants.TAG, "${adapter.getCurrentVoice()}")
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
        val date = adapter.model.getCurrentPhoto().created?.toDate()?: Date()
        val dateStr: String = sdf.format(date)
        playerFilename.text = dateStr
        playerHeader.text = "Playing"
        //Play the audio
        isPlaying = true
        mediaPlayer!!.setOnCompletionListener(OnCompletionListener {
            stopAudio()
            playerHeader.text = "Finished"
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


    /***************************************************************************************************************************/
    companion object{
        const val fragmentName = "PhotoFragment"
    }



    class PhotoAdapter(val fragment: PhotoFragment) : RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {
            val model = ViewModelProvider(fragment.requireActivity()).get(PhotoVoiceViewModel::class.java)
//        val url = "http://........" // your URL here
//        val mediaPlayer = MediaPlayer().apply {
//            setAudioAttributes(
//                AudioAttributes.Builder()
//                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
//                    .setUsage(AudioAttributes.USAGE_MEDIA)
//                    .build()
//            )
//            setDataSource(url)
//            prepare() // might take long! (for buffering, etc)
//            start()
//        }


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transform,parent,false)
            return PhotoViewHolder(view)
        }

        override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
            holder.bind(model.getPhotoVoiceAt(position))
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
        fun getCurrentVoice()= model.getCurrentPhoto().voice
        fun deletePhoto(position: Int) {
            model.updatePos(position)
            model.removeCurrentPhoto()
            val toast = Toast.makeText(fragment.context,"delete photoVoice successfully", Toast.LENGTH_SHORT)
            toast.show()
        }


        inner class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            val imageView: ImageView = itemView.findViewById(R.id.image_view_item_transform)
            val textView: TextView = itemView.findViewById(R.id.text_view_item_transform)
            val playButton: MaterialButton = itemView.findViewById(R.id.play_button)
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
                playButton.setOnClickListener {
                    model.updatePos(absoluteAdapterPosition)
                    if (fragment.isPlaying) {
                        fragment.stopAudio()
                        fragment.playAudio()
                    } else {
                        fragment.playAudio()
                    }
                }

            }

            fun bind(photoVoice: PhotoVoice) {
                val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.US)
                val date = photoVoice.created?.toDate()?: Date()
                val dateStr: String = sdf.format(date)
                textView.text = dateStr + "\n"+photoVoice.location
                imageView.load(photoVoice.photo) {
                    crossfade(true)
                    transformations(CircleCropTransformation())
                }
            }

        }

    }
}




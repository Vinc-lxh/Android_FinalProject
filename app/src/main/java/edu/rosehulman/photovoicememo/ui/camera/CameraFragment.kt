package edu.rosehulman.photovoicememo.ui.camera

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import edu.rosehulman.photovoicememo.databinding.FragmentCameraBinding
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Environment
import android.os.SystemClock
import android.util.Log
import android.widget.*

import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import edu.rosehulman.photovoicememo.BuildConfig
import edu.rosehulman.photovoicememo.Constants
import edu.rosehulman.photovoicememo.R
import edu.rosehulman.photovoicememo.model.PhotoVoice
import edu.rosehulman.photovoicememo.model.PhotoVoiceViewModel
import edu.rosehulman.photovoicememo.ui.Photo.PhotoViewModel
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random


class CameraFragment : Fragment() {

    private lateinit var photoViewModel: PhotoVoiceViewModel
    private lateinit var binding: FragmentCameraBinding
    private lateinit var mediaRecorder: MediaRecorder
    private lateinit var navController: NavController
    private var isRecording: Boolean = false
    private var recordFile: String = ""
    private lateinit var doneBtn: ImageButton
    private lateinit var recordBtn: ImageButton
    private lateinit var timer: Chronometer
    lateinit var imageView:ImageView
    private var latestTmpUri: Uri? = null

    private val storageImagesRef = Firebase.storage
        .reference
        .child("images")

    private val storageRecordRef = Firebase.storage
        .reference
        .child("recording")

    private var storageUriStringInFragment: String = ""


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCameraBinding.inflate(inflater, container, false)
        photoViewModel = ViewModelProvider(requireActivity()).get(PhotoVoiceViewModel::class.java)
        takeImage()
        showPictureDialog()
        return binding.root
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



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Intitialize Variables
        navController = Navigation.findNavController(view)
        doneBtn = view.findViewById(R.id.done_record_button)
        recordBtn = view.findViewById(R.id.record_button)
        timer = view.findViewById(R.id.record_timer)

        setupButton()

    }


    private fun checkRecordPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun stopRecording() {
        timer.stop()
        //Stop media recorder and set it to null for further use to record new audio
        mediaRecorder.stop()
        mediaRecorder.release()
        //mediaRecorder = null
    }

    private fun startRecording() {

        //Start timer from 0
        timer.base = SystemClock.elapsedRealtime()
        timer.start()

        //Get app external directory path
        val recordPath = requireActivity()!!.getExternalFilesDir("/")!!.absolutePath

        //Get current date and time
        val formatter = SimpleDateFormat("yyyy_MM_dd_hh_mm_ss", Locale.CANADA)
        val now = Date()

        recordFile = "Recording_" + formatter.format(now) + ".3gp"


        //Setup Media Recorder for recording
        val context:android.content.Context = requireContext()
        mediaRecorder = MediaRecorder(context)
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        mediaRecorder.setOutputFile("$recordPath/$recordFile")
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

        try {
            mediaRecorder.prepare()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        mediaRecorder.start()
    }

    private fun uploadRecording(){
        val recordPath = requireActivity()!!.getExternalFilesDir("/")!!.absolutePath
        var file = Uri.fromFile(File("$recordPath/$recordFile"))
        val riversRef = storageRecordRef.child("${file.lastPathSegment}")
        var  uploadTask = riversRef.putFile(file)

// Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads
            Log.d(Constants.TAG, "uploaded recording")
        }.addOnSuccessListener { taskSnapshot ->
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            // ...
        }
    }

    //camera part---------------------------------------------------------------------------------------------------------------------------

    private val takeImageResult =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
            if (isSuccess) {
                latestTmpUri?.let { uri ->
                    binding.cameraImageView.setImageURI(uri)
                    addPhotoFromUri(uri)
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

    private fun showPictureDialog() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Voice Memo Option")
        builder.setMessage("Would you like to record a short voice memo?\n")
        builder.setPositiveButton("Yes") { _, _ ->

        }
        builder.setNegativeButton("NO, just photo") { _, _ ->
            navController.navigate(R.id.nav_photo_detail)
        }
        builder.create().show()
    }

//    private fun takeImage() {
//        lifecycleScope.launchWhenStarted {
//            getTmpFileUri().let { uri ->
//                latestTmpUri = uri
//                takeImageResult.launch(uri)
//            }
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



    private fun addPhotoFromUri(uri: Uri?) {
        if (uri == null) {
            Log.e(Constants.TAG, "Uri is null. Not saving to storage")
            return
        }
// https://stackoverflow.com/a/5657557
        val stream = requireActivity().contentResolver.openInputStream(uri)
        if (stream == null) {
            Log.e(Constants.TAG, "Stream is null. Not saving to storage")
            return
        }

        // TODO: Add to storage
        val imageId = Math.abs(Random.nextLong()).toString()

        storageImagesRef.child(imageId).putStream((stream))
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                storageImagesRef.child(imageId).downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    storageUriStringInFragment = task.result.toString()
                    photoViewModel.addPhotoVoice(PhotoVoice(photo = storageUriStringInFragment))
                    Log.d(Constants.TAG, "Got download uri: $storageUriStringInFragment")
                } else {
                    // Handle failures
                    // ...
                }
            }
    }

    private fun setupButton() {
        binding.doneRecordButton.setOnClickListener{
            if (isRecording) {
                val alertDialog = AlertDialog.Builder(context)
                alertDialog.setPositiveButton(
                    "OKAY"
                ) { dialog, which ->
                    navController.navigate(R.id.nav_photo_detail)
                    isRecording = false
                }
                alertDialog.setNegativeButton("CANCEL", null)
                alertDialog.setTitle("Audio Still recording")
                alertDialog.setMessage("Are you sure, you want to stop the recording?")
                alertDialog.create().show()

            } else {
                uploadRecording()
                navController.navigate(R.id.nav_photo_detail)
            }
        }
        binding.recordButton.setOnClickListener{
            if(isRecording){//double hit stop recording
                stopRecording()
                // Change button image and set Recording state to false
                recordBtn.setImageDrawable(
                    resources.getDrawable(
                        R.drawable.ic_baseline_mic_24,
                        null
                    )
                )
                isRecording = false;
            }else{
                if(checkRecordPermission()){
                    startRecording()
                    recordBtn.setImageDrawable(resources.getDrawable(
                        R.drawable.ic_baseline_stop_24,
                        null
                    )
                    )
                    isRecording = true;
                }else{
                    permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                }

            }
        }
    }


}
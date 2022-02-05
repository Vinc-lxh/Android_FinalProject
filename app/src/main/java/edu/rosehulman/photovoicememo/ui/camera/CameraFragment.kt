package edu.rosehulman.photovoicememo.ui.camera

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import edu.rosehulman.photovoicememo.MainActivity
import edu.rosehulman.photovoicememo.databinding.FragmentCameraBinding
import java.util.jar.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.SystemClock
import android.util.Log
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import edu.rosehulman.photovoicememo.R
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class CameraFragment : Fragment() {

    private lateinit var cameraViewModel: CameraViewModel
    private lateinit var binding: FragmentCameraBinding
    private lateinit var mediaRecorder: MediaRecorder
    private lateinit var navController: NavController
    private var isRecording: Boolean = false
    private var recordFile: String = ""
    private lateinit var doneBtn: ImageButton
    private lateinit var recordBtn: ImageButton
    private lateinit var filenameText: TextView
    private lateinit var timer: Chronometer
    // This property is only valid between onCreateView and
    // onDestroyView.
    lateinit var imageView:ImageView
    lateinit var openButton: Button
    lateinit var cam_uri: Uri
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCameraBinding.inflate(inflater, container, false)
        permissionLauncher.launch(android.Manifest.permission.CAMERA)
//        permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
        permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        pickCamera()
        cameraViewModel =
            ViewModelProvider(requireActivity()).get(CameraViewModel::class.java)
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

    private fun checkRecordPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.RECORD_AUDIO
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

    fun pickCamera() {
        imageView = binding.cameraImageView
//        openButton = binding.openButton
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera")
        cam_uri = requireContext().contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        )!!
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cam_uri)
        startCamera.launch(cameraIntent)
    }

    var startCamera = registerForActivityResult(
        StartActivityForResult()
    ) { result ->
        if (result.getResultCode() === RESULT_OK) {
            Log.d("errorPhoto", "is ok")
            imageView.setImageURI(cam_uri)
        }else{
            Log.d("errorPhoto", "is not ok")
        }
    }



}
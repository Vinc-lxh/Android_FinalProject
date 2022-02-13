package edu.rosehulman.photovoicememo.ui.camera

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
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
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Environment
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService

import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.gms.location.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import edu.rosehulman.photovoicememo.BuildConfig
import edu.rosehulman.photovoicememo.Constants
import edu.rosehulman.photovoicememo.MainActivity
import edu.rosehulman.photovoicememo.R
import edu.rosehulman.photovoicememo.model.PhotoVoice
import edu.rosehulman.photovoicememo.model.PhotoVoiceViewModel
import edu.rosehulman.photovoicememo.ui.Photo.PhotoFragment
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
    private var startRecording: Boolean = false
    private var isRecording: Boolean = false
    private var recordFile: String = ""
    private lateinit var doneBtn: ImageButton
    private lateinit var recordBtn: ImageButton
    private lateinit var timer: Chronometer
    lateinit var imageView:ImageView
    private var loc: String = ""
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    val PERMISSION_ID = 2765
    private val storageImagesRef = Firebase.storage
        .reference
        .child("images")

    private val storageRecordRef = Firebase.storage
        .reference
        .child("recording")

    private var storageUriStringInFragment: String = ""
    private var voiceUriStringInFragment: String = ""

    companion object{
        const val fragmentName = "CameraFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setFragmentResultListener("requestKey") { requestKey, bundle ->
            val result = bundle.getString("bundleKey")
            //Log.d(edu.rosehulman.photovoicememo.model.Constants.TAG,"uri getted is $result")
            //Log.d(edu.rosehulman.photovoicememo.model.Constants.TAG,"uri getted to Uri is ${result?.toUri()}")
            if (result != null) {
                binding.cameraImageView.setImageURI(result.toUri())
                addPhotoFromUri(result.toUri())
            }

        }
        binding = FragmentCameraBinding.inflate(inflater, container, false)
        photoViewModel = ViewModelProvider(requireActivity()).get(PhotoVoiceViewModel::class.java)
        photoViewModel.addListener(fragmentName){

        }
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        getLastLocation()
        setupButton()
        return binding.root
    }
//    override fun onDestroyView() {
//        super.onDestroyView()
//        photoViewModel.removeListener(PhotoFragment.fragmentName)
//    }
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
        startRecording = true
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

        var  uploadTask = riversRef.putFile(file).continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
            storageRecordRef.child("${file.lastPathSegment}").downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    voiceUriStringInFragment = task.result.toString()
                    Log.d(Constants.TAG, "Got download uri: $voiceUriStringInFragment")
                    photoViewModel.addPhotoVoice(PhotoVoice(photo = storageUriStringInFragment,voice = voiceUriStringInFragment, albumID = photoViewModel.getCurrentAlbum().id, location = loc ))
                } else {
                    Log.d(Constants.TAG,"failureHere")
                }
            }
        uploadTask.addOnFailureListener {

            Log.d(Constants.TAG, "uploaded recording")
        }.addOnSuccessListener { taskSnapshot ->
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            // ...
        }
    }

    //camera part---------------------------------------------------------------------------------------------------------------------------

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

        storageImagesRef.child(imageId).putStream(stream)
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
                    "OKAY",

                ) { dialog, which ->
                    stopRecording()
                    uploadRecording()
//                    photoViewModel.addPhotoVoice(PhotoVoice(photo = storageUriStringInFragment,voice = voiceUriStringInFragment, albumID = photoViewModel.getCurrentAlbum().id))
                    navController.navigate(R.id.nav_photo)
                    isRecording = false
                }
                alertDialog.setNegativeButton("CANCEL", null)
                alertDialog.setTitle("Audio Still recording")
                alertDialog.setMessage("Are you sure, you want to stop the recording?")
                alertDialog.create().show()

            }else if(startRecording == false){
                photoViewModel.addPhotoVoice(PhotoVoice(photo = storageUriStringInFragment,voice = "", albumID = photoViewModel.getCurrentAlbum().id, location = loc))
                navController.navigate(R.id.nav_photo)
            }
            else {
//                loc= (activity as MainActivity).getLastLocation()
//                if(loc.equals("")){
//                    loc = (activity as MainActivity).getLastLocation()
//                }
//                loc = (activity as MainActivity).outputText
//                Log.d(Constants.TAG, "location is $loc")
                uploadRecording()
                navController.navigate(R.id.nav_photo)
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
    //--------------------------------------------GPS

    fun CheckPermission():Boolean{
        //this function will return a boolean
        //true: if we have permission
        //false if not
        if(
            ActivityCompat.checkSelfPermission(requireContext(),android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(requireContext(),android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ){
            return true
        }

        return false
    }

    @SuppressLint("MissingPermission")
    fun getLastLocation() :String{
        if(CheckPermission()){
            if(isLocationEnabled()){
                Log.d("locationCheck" ,"permission granted")
                fusedLocationProviderClient.lastLocation.addOnCompleteListener {task->
                    var location: Location? = task.result
                    if(location == null){
                        NewLocationData()
                    }else{
                        loc = getCityName(location.latitude, location.longitude)
                    }
                }
            }else{
                Toast.makeText(context,"Please Turn on Your device Location",Toast.LENGTH_SHORT).show()
            }
        }else{
            RequestPermission()
        }

        return loc
    }

    @SuppressLint("MissingPermission")
    fun NewLocationData(){
        var locationRequest =  LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 0
        locationRequest.fastestInterval = 0
        locationRequest.numUpdates = 1
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        fusedLocationProviderClient!!.requestLocationUpdates(
            locationRequest,locationCallback, Looper.myLooper()
        )
    }
    private val locationCallback = object : LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult) {
            var lastLocation: Location = locationResult.lastLocation
            Log.d("locationCheck","your last last location: "+ lastLocation.longitude.toString())
        }
    }

    private fun getCityName(lat: Double,long: Double):String{
        var cityName:String = ""
        var geoCoder = Geocoder(requireContext(), Locale.getDefault())
        var Adress = geoCoder.getFromLocation(lat,long,3)
        cityName = Adress.get(0).locality +", "+ Adress.get(0).countryName
        return cityName
    }


    fun RequestPermission(){
        //this function will allows us to tell the user to requesut the necessary permsiion if they are not garented
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION,android.Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_ID
        )
    }
    fun isLocationEnabled():Boolean{
        //this function will return to us the state of the location service
        //if the gps or the network provider is enabled then it will return true otherwise it will return false
        var locationManager =  requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER)
    }

}
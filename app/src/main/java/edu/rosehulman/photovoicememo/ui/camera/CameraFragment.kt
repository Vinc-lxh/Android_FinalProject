package edu.rosehulman.photovoicememo.ui.camera

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
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
import android.net.Uri
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult


class CameraFragment : Fragment() {

    private lateinit var cameraViewModel: CameraViewModel
    private lateinit var binding: FragmentCameraBinding

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
        pickCamera()
        cameraViewModel =
            ViewModelProvider(requireActivity()).get(CameraViewModel::class.java)
        return binding.root
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
        StartActivityForResult(),
        ActivityResultCallback<ActivityResult> { result ->
            if (result.getResultCode() === RESULT_OK) {
                imageView.setImageURI(cam_uri)
            }
        })


    companion object {
        private const val REQUEST_CODE = 42
    }


}
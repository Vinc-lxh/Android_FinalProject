package edu.rosehulman.photovoicememo.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import edu.rosehulman.photovoicememo.R
import edu.rosehulman.photovoicememo.databinding.FragmentPlayRecordingBinding
import edu.rosehulman.photovoicememo.databinding.FragmentProfileBinding
import edu.rosehulman.photovoicememo.ui.profile.ProfileViewModel

class PlayRecordingFragment : Fragment() {
    private lateinit var binding: FragmentPlayRecordingBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentPlayRecordingBinding.inflate(inflater, container, false)
        return binding.root
    }


}
package edu.rosehulman.photovoicememo.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import edu.rosehulman.photovoicememo.databinding.FragmentProfileBinding
import coil.load
import coil.transform.CircleCropTransformation

class ProfileFragment : Fragment() {
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var binding: FragmentProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
        ): View {
            profileViewModel =
                ViewModelProvider(this).get(ProfileViewModel::class.java)
    
            binding = FragmentProfileBinding.inflate(inflater, container, false)
            binding.centerImage.load("https://scontent-iad3-1.xx.fbcdn.net/v/t1.6435-9/38223814_102956127310567_7245926139808448512_n.jpg?_nc_cat=104&ccb=1-5&_nc_sid=09cbfe&_nc_ohc=BnQY9BXARkEAX_pjpj9&_nc_ht=scontent-iad3-1.xx&oh=00_AT-fqA8DsIiauvBpho4TmCHLbP5BonUH4y2BduXxg2Cphg&oe=62010BBD") {
            crossfade(true)
            transformations(CircleCropTransformation())
        }

            return binding.root
        }


}
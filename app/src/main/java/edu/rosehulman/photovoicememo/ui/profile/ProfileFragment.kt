package edu.rosehulman.photovoicememo.ui.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import edu.rosehulman.photovoicememo.databinding.FragmentProfileBinding
import coil.load
import coil.transform.CircleCropTransformation
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.rosehulman.photovoicememo.R
import edu.rosehulman.photovoicememo.model.Constants
import edu.rosehulman.photovoicememo.model.UserViewModel

class ProfileFragment : Fragment() {
    private lateinit var profileViewModel: UserViewModel
    private lateinit var binding: FragmentProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
        ): View {
            profileViewModel =
                ViewModelProvider(this).get(UserViewModel::class.java)
    
            binding = FragmentProfileBinding.inflate(inflater, container, false)

            val ref = Firebase.firestore.collection("photoVoice")
            ref.addSnapshotListener { snapshot: QuerySnapshot?, error: FirebaseFirestoreException? ->
                error?.let {
                    Log.d(Constants.TAG, "Error: $error")
                    return@addSnapshotListener
                }
                snapshot?.documents?.forEach {
                    Log.d(Constants.TAG, "detail: $it")

                }
            }
                binding.editButton.setOnClickListener {
                    findNavController().navigate(R.id.nav_user_edit)

                }
             binding.logoutButton.setOnClickListener{
                    Firebase.auth.signOut()

                }
            setHasOptionsMenu(true)
            updateView()
            return binding.root
        }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.action_tips -> {
                findNavController().navigate(R.id.nav_tips,
                    null,
                    navOptions{
                        anim{
                            enter = android.R.anim.slide_in_left
                            exit = android.R.anim.slide_out_right
                        }
                    }
                )
                return true
            }else -> super.onOptionsItemSelected(item)
        }
    }
    private fun updateView(){
        profileViewModel.getOrMakeUser {
            with(profileViewModel.user!!) {
                Log.d(Constants.TAG, "$this")
                binding.userName.setText(name)
                binding.userAge.setText(age.toString())
                binding.userEmail.setText(email)
                if(storageUriString.isNotEmpty()){
                    binding.centerImage.load(storageUriString){
                        crossfade(true)
                        transformations(CircleCropTransformation())
                    }
                }

            }
        }
    }

}
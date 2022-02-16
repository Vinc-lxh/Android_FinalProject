package edu.rosehulman.photovoicememo.model

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.rosehulman.photovoicememo.Constants
import edu.rosehulman.photovoicememo.User

class UserViewModel: ViewModel() {
    var ref = Firebase.firestore.collection(User.COLLECTION_PATH).document(Firebase.auth.uid!!)
    var user: User? = null
    fun resetUser(){
        user = null
        Log.d(Constants.TAG,"user reset $user")
    }
    fun hasCompletedSetup() = user?.hasCompletedSetup ?: false

    fun getOrMakeUser(observer: () -> Unit){
        Log.d(Constants.TAG,"User: starting $user")
        ref = Firebase.firestore.collection(User.COLLECTION_PATH).document(Firebase.auth.uid!!)
        if(user != null){
            //get
            observer()

        }else{
            //make
            Log.d(Constants.TAG,"User: make here!")
            ref.get().addOnSuccessListener { snapshot: DocumentSnapshot ->
                if(snapshot.exists()){
                    user = snapshot.toObject(User::class.java)
                    Log.d(Constants.TAG,"User: 29")
                }else{
                    user = User(name = Firebase.auth.currentUser!!.displayName!!)
                    ref.set(user!!)
                    Log.d(Constants.TAG,"User: 32")
                }
                observer()
            }
        }
    }
    fun update(newName: String, newAge: Int, newEmail: String, newStorageUriString: String,
               newHasCompletedSetup: Boolean){
        ref = Firebase.firestore.collection(User.COLLECTION_PATH).document(Firebase.auth.uid!!)
        if(user!=null){
            with(user!!){
                name = newName
                age = newAge
                email = newEmail
                storageUriString = newStorageUriString
                hasCompletedSetup = newHasCompletedSetup
                ref.set(this)
            }
        }
    }

}
package edu.rosehulman.photovoicememo.model

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.rosehulman.photovoicememo.User

class UserViewModel: ViewModel() {
    var ref = Firebase.firestore.collection(User.COLLECTION_PATH).document(Firebase.auth.uid!!)
    var user: User? = null

    fun hasCompletedSetup() = user?.hasCompletedSetup ?: false

    fun getOrMakeUser(observer: () -> Unit){
        ref = Firebase.firestore.collection(User.COLLECTION_PATH).document(Firebase.auth.uid!!)
        if(user != null){
            //get
            observer()

        }else{
            //make
            ref.get().addOnSuccessListener { snapshot: DocumentSnapshot ->
                if(snapshot.exists()){
                    user = snapshot.toObject(User::class.java)
                }else{
                    user = User(name = Firebase.auth.currentUser!!.displayName!!)
                    ref.set(user!!)
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
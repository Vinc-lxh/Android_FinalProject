package edu.rosehulman.photovoicememo.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp

data class PhotoVoice(
    var photo: String = "", var voice: String = ""
) {
    @get:Exclude
    var id = ""

    @ServerTimestamp
    var created: Timestamp? = null

    companion object{
        const val COLLECTION_PATH = "photo"
        const val CREATED_KEY = "created"

        fun from(snapshot: DocumentSnapshot): PhotoVoice{
            val mq = snapshot.toObject(PhotoVoice::class.java)!!
            mq.id = snapshot.id
            return mq
        }
    }

}
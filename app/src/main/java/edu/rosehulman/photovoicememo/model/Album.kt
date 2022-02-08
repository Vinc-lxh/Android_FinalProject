package edu.rosehulman.photovoicememo.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp

data class Album(
        var name: String = "",var url: String = ""
) {
    @get:Exclude
    var id = ""

    @ServerTimestamp
    var created: Timestamp? = null

    companion object{
        const val ALBUM_PATH = "Albums"
        const val defaultAlbumPage = "https://lh3.googleusercontent.com/pw/AM-JKLXqLd8kNbQDtlKC-ycxNz7iSkCVVYKp6QUr3K6kL3B66NZCJXgFJKdlRq_rdWfCIK6eL8c3D4jWx2mTliYbBBJG5iZ_e5Q8FGwoxYIWElzpXfVFxH1NNmkflM391NJ6sNKLq_AuCs_jBmr0g786VqRT=w860-h981-no"
        fun from(snapshot: DocumentSnapshot): Album{
            val mq = snapshot.toObject(Album::class.java)!!
            mq.id = snapshot.id
            return mq
        }
    }

}
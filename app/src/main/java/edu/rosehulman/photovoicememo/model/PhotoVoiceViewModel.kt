package edu.rosehulman.photovoicememo.model

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.rosehulman.photovoicememo.User
import edu.rosehulman.photovoicememo.model.Album.Companion.defaultAlbumPage
import kotlin.random.Random

class PhotoVoiceViewModel: ViewModel() {
    private var photosvoice = ArrayList<PhotoVoice>()
    private var albnum = arrayListOf<Album>()
    var currentPos = 0
    var albumPos = 0
    lateinit var ref: CollectionReference
    lateinit var albumRef: CollectionReference

    val subscriptions = HashMap<String, ListenerRegistration>()
    fun addListener(fragmentName: String, observer: () -> Unit) {
        val uid = Firebase.auth.currentUser!!.uid
        ref = Firebase.firestore.collection(User.COLLECTION_PATH).document(uid)
            .collection(PhotoVoice.COLLECTION_PATH)

        Log.d(Constants.TAG,"Adding listener for $fragmentName")
        val subscription = ref
            .whereEqualTo("albumID",getCurrentAlbum().id)
            .orderBy(PhotoVoice.CREATED_KEY, Query.Direction.ASCENDING)
            .addSnapshotListener{ snapshot: QuerySnapshot?, error: FirebaseFirestoreException?->
                error?.let{
                    Log.d(Constants.TAG,"Error: $error")
                    return@addSnapshotListener
                }
                photosvoice.clear()
                snapshot?.documents?.forEach {
                    photosvoice.add(PhotoVoice.from(it))
                }
                if(photosvoice.size>0&&getCurrentAlbum().url.equals(defaultAlbumPage)){
                    updateCurrentAlbumPage(photosvoice[0].photo)
                }
                observer()
            }
        subscriptions[fragmentName] = subscription
    }

    fun addAlbumListener(fragmentName: String, observer: () -> Unit) {
        val uid = Firebase.auth.currentUser!!.uid
        albumRef = Firebase.firestore.collection(User.COLLECTION_PATH).document(uid)
            .collection(Album.ALBUM_PATH)
        Log.d(Constants.TAG,"Adding Album listener for $fragmentName")
        val subscription = albumRef
            .orderBy(PhotoVoice.CREATED_KEY, Query.Direction.ASCENDING)
            .addSnapshotListener{ snapshot: QuerySnapshot?, error: FirebaseFirestoreException?->
                error?.let{
                    Log.d(Constants.TAG,"Error: $error")
                    return@addSnapshotListener
                }
                albnum.clear()
                snapshot?.documents?.forEach {
                    albnum.add(Album.from(it))
                }
                observer()
            }

        subscriptions[fragmentName] = subscription
    }

//photo function
    fun getPhotoVoiceAt(position: Int):PhotoVoice{
        return photosvoice[position]
    }
    fun getCurrentPhoto() = getPhotoVoiceAt(currentPos)
    fun addPhotoVoice(photoVoice: PhotoVoice){
        val p = photoVoice ?: PhotoVoice(useGivenOrRandomCaption(""),useGivenOrRandom(""))
        ref.add(p)
    }
    fun updateCurrentPhoto(cap:String, url: String){
        photosvoice[currentPos].photo = useGivenOrRandomCaption(cap)
        photosvoice[currentPos].voice = useGivenOrRandom(url)
        ref.document(getCurrentPhoto().id).set(getCurrentPhoto())
    }
    fun removeCurrentPhoto(){
        ref.document(getCurrentPhoto().id).delete()
        currentPos = 0
    }
    fun updatePos(pos: Int){
        currentPos = pos
    }
    fun size() = photosvoice.size
    //album function

    fun getAlbumAt(position: Int):Album{
        return albnum[position]
    }
    fun getCurrentAlbum() = getAlbumAt(albumPos)
    fun addAlbum(alb: Album){
        albumRef.add(alb)
    }
    fun updateCurrentAlbum(album:Album){
        albnum[albumPos]= album
        albumRef.document(getCurrentAlbum().id).set(album)
    }
    fun updateCurrentAlbumPage(Pageurl:String){
        albnum[albumPos].url = Pageurl
        albumRef.document(getCurrentAlbum().id).set(albnum[albumPos])
    }
    fun removeCurrentAlbum(){
        albumRef.document(getCurrentAlbum().id).delete()
        albumPos = 0
    }
    fun updateAlbumPos(pos: Int){
        albumPos = pos
    }
    fun Albumsize() = albnum.size


    fun useGivenOrRandom(given: String): String {
        if (given.isNotBlank()) {
            return given
        }
        val idx = Random.nextInt(urls.size)
        return urls[idx]
    }

    fun useGivenOrRandomCaption(given: String): String {
        if (given.isNotBlank()) {
            return given
        }
        val idx = Random.nextInt(captions.size)
        return captions[idx]
    }

    fun removeListener(fragmentName: String) {
        Log.d(Constants.TAG,"removing listener for $fragmentName")
        subscriptions[fragmentName]?.remove()// this tell firebase to step listening
        subscriptions.remove(fragmentName)
    }


    companion object {
        val captions = arrayListOf(
            "asfasffasdf",
            "98ashdfasdf",
            "asdfnlajfo23",
            "ojoijoasdf",
            "238ahfaosdfjo2i3r",

            )


        val urls = arrayListOf(
            "https://cdn.britannica.com/91/181391-050-1DA18304/cat-toes-paw-number-paws-tiger-tabby.jpg",
            "https://upload.wikimedia.org/wikipedia/commons/b/b1/VAN_CAT.png",
            "https://cdn.britannica.com/68/160068-050-53FE2889/Snowshoe-cat.jpg",
//
//            "https://commons.wikimedia.org/wiki/Category:Architecture_by_city#/media/File:Ray_and_Maria_Stata_Center_(MIT).JPG",
//            "https://commons.wikimedia.org/wiki/Category:Stained-glass_windows_in_Bratislava#/media/File:Bratislavastainedglass.JPG",
//            "https://commons.wikimedia.org/wiki/Category:Stained-glass_windows_in_Bratislava#/media/File:COA_Palugyay_Ferenc.jpg",       "https://commons.wikimedia.org/wiki/Category:Stained-glass_windows_in_Bratislava#/media/File:Frantiskansky_kostol37.jpg",
//            "https://commons.wikimedia.org/wiki/Category:Stained-glass_windows_in_Athens#/media/File:St._Dionysios_Catholic_-_Vitrail,_2005.jpg",
//
            //
        )
    }

}
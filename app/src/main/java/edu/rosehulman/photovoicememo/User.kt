package edu.rosehulman.photovoicememo

data class User(
    var name: String = "",
    var age: Int = -1,
    var email: String = "unknown",
    var storageUriString: String = "",
    var hasCompletedSetup: Boolean = false
){
    companion object{
        const val COLLECTION_PATH = "users"
    }
}

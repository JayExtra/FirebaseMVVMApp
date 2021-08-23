package com.dev.james.firebasemvvm.repository.firebase

import com.google.firebase.auth.FirebaseUser

interface BaseAuthenticator {

    //this class will implement all the basic authentication api calls. Using this method of abstraction
    //will allow us to implement any type of authentication api may it be
    //that you have built your own or Firebase auth sdks. This makes it easy to swap
    //in the future because all authentication classes you will use will inherit behaviour from this base class.
    //Also in testing it will make it easy to swap your actual sdks with fake ones

    suspend fun signUpWithEmailPassword(email:String , password:String) : FirebaseUser?

    suspend fun signInWithEmailPassword(email: String , password: String):FirebaseUser?

    fun signOut() : FirebaseUser?

    fun getUser() : FirebaseUser?

    suspend fun sendPasswordReset(email :String)
}
package com.dev.james.firebasemvvm.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.james.firebasemvvm.repository.BaseAuthRepository
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository : BaseAuthRepository
) : ViewModel() {

    private val TAG = "MainViewModel"

    /**This is a ViewModel class and is responsible for the logic of all ui.
     * It shall be shared with the three fragments.
     * Only share ViewModels when the fragments share a feature or functionality */

    //create the auth state livedata object that will be passed to
    //the home fragment and shall be used to control the ui i.e show authentication state
    //control behaviour of sign in and sign up button
    private val _firebaseUser = MutableLiveData<FirebaseUser?>()
    val currentUser get() = _firebaseUser

    //create our channels that will be used to pass messages to the main ui
    //create event channel
    private val eventsChannel = Channel<AllEvents>()
    //the messages passed to the channel shall be received as a Flowable
    //in the ui
    val allEventsFlow = eventsChannel.receiveAsFlow()


    //validate all fields first before performing any sign in operations
    fun signInUser(email: String , password: String) = viewModelScope.launch{
        when {
            email.isEmpty() -> {
                eventsChannel.send(AllEvents.ErrorCode(1))
            }
            password.isEmpty() -> {
                eventsChannel.send(AllEvents.ErrorCode(2))
            }
            else -> {
                actualSignInUser(email , password)
            }
        }
    }

    //validate all fields before performing any sign up operations
    fun signUpUser(email : String , password: String , confirmPass : String)= viewModelScope.launch {
        when{
            email.isEmpty() -> {
                eventsChannel.send(AllEvents.ErrorCode(1))
            }
            password.isEmpty() -> {
                eventsChannel.send(AllEvents.ErrorCode(2))
            }
            password != confirmPass ->{
                eventsChannel.send(AllEvents.ErrorCode(3))
            }
            else -> {
                actualSignUpUser(email, password)
            }
        }
    }


    private fun actualSignInUser(email:String, password: String) = viewModelScope.launch {
        try {
            val user = repository.signInWithEmailPassword(email, password)
            user?.let {
                _firebaseUser.postValue(it)
                eventsChannel.send(AllEvents.Message("login success"))
            }
        }catch(e:Exception){
            val error = e.toString().split(":").toTypedArray()
            Log.d(TAG, "signInUser: ${error[1]}")
            eventsChannel.send(AllEvents.Error(error[1]))
        }
    }

    private fun actualSignUpUser(email:String , password: String) = viewModelScope.launch {
        try {
            val user = repository.signUpWithEmailPassword(email, password)
            user?.let {
                _firebaseUser.postValue(it)
                eventsChannel.send(AllEvents.Message("sign up success"))
            }
        }catch(e:Exception){
            val error = e.toString().split(":").toTypedArray()
            Log.d(TAG, "signInUser: ${error[1]}")
            eventsChannel.send(AllEvents.Error(error[1]))
        }
    }

    fun signOut() = viewModelScope.launch {
        try {
            val user = repository.signOut()
            user?.let {
                eventsChannel.send(AllEvents.Message("logout failure"))
            }?: eventsChannel.send(AllEvents.Message("sign out successful"))

            getCurrentUser()

        }catch(e:Exception){
            val error = e.toString().split(":").toTypedArray()
            Log.d(TAG, "signInUser: ${error[1]}")
            eventsChannel.send(AllEvents.Error(error[1]))
        }
    }

    fun getCurrentUser() = viewModelScope.launch {
        val user = repository.getCurrentUser()
        _firebaseUser.postValue(user)
    }

    fun verifySendPasswordReset(email: String){
        if(email.isEmpty()){
            viewModelScope.launch {
                eventsChannel.send(AllEvents.ErrorCode(1))
            }
        }else{
            sendPasswordResetEmail(email)
        }

    }

    private fun sendPasswordResetEmail(email: String) = viewModelScope.launch {
        try {
            val result = repository.sendResetPassword(email)
            if (result){
                eventsChannel.send(AllEvents.Message("reset email sent"))
            }else{
                eventsChannel.send(AllEvents.Error("could not send password reset"))
            }
        }catch (e : Exception){
            val error = e.toString().split(":").toTypedArray()
            Log.d(TAG, "signInUser: ${error[1]}")
            eventsChannel.send(AllEvents.Error(error[1]))
        }
    }



    sealed class AllEvents {
        data class Message(val message : String) : AllEvents()
        data class ErrorCode(val code : Int):AllEvents()
        data class Error(val error : String) : AllEvents()
    }
}
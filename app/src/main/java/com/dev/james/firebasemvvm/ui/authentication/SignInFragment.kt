package com.dev.james.firebasemvvm.ui.authentication

import android.graphics.Color.RED
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.dev.james.firebasemvvm.R
import com.dev.james.firebasemvvm.databinding.FragmentSigninBinding
import com.dev.james.firebasemvvm.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignInFragment : Fragment(R.layout.fragment_signin) {
    private val viewModel : MainViewModel by activityViewModels()
    private var _binding : FragmentSigninBinding? = null
    private val binding get() = _binding
    private val TAG = "SignInFragment"
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSigninBinding.inflate(inflater , container , false)
        listenToChannels()
        registerObservers()
        binding?.apply {
            signInButton.setOnClickListener {
                progressBarSignin.isVisible = true
                val email = userEmailEtv.text.toString()
                val password = userPasswordEtv.text.toString()
                viewModel.signInUser(email, password)
            }

            signUpTxt.setOnClickListener {
                findNavController().navigate(R.id.action_signInFragment_to_signUpFragment)
            }

            forgotPassTxt.setOnClickListener {
                findNavController().navigate(R.id.action_signInFragment_to_resetPasswordFragment)
            }
        }
        return binding?.root
    }

    private fun registerObservers() {
        viewModel.currentUser.observe(viewLifecycleOwner, { user ->
            user?.let {
               findNavController().navigate(R.id.action_signInFragment_to_homeFragment2)
            }
        })
    }

    private fun listenToChannels() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allEventsFlow.collect { event ->
                when(event){
                    is MainViewModel.AllEvents.Error -> {
                        binding?.apply {
                            errorTxt.text =  event.error
                            progressBarSignin.isInvisible = true
                        }
                    }
                    is MainViewModel.AllEvents.Message -> {
                        Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
                    }
                    is MainViewModel.AllEvents.ErrorCode -> {
                        if (event.code == 1)
                            binding?.apply {
                                userEmailEtvl.error = "email should not be empty"
                                progressBarSignin.isInvisible = true
                            }


                        if(event.code == 2)
                            binding?.apply {
                                userPasswordEtvl.error = "password should not be empty"
                                progressBarSignin.isInvisible = true
                            }
                    }

                    else ->{
                        Log.d(TAG, "listenToChannels: No event received so far")
                    }
                }

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
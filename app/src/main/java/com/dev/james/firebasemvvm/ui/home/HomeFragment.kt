package com.dev.james.firebasemvvm.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.dev.james.firebasemvvm.R
import com.dev.james.firebasemvvm.databinding.FragmentHomeBinding
import com.dev.james.firebasemvvm.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding : FragmentHomeBinding? = null
    private val binding get() = _binding
    private val viewModel : MainViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        getUser()
        registerObserver()
        listenToChannels()
        return binding?.root
    }



    private fun getUser() {
        viewModel.getCurrentUser()
    }

    private fun listenToChannels() {
        viewLifecycleOwner.lifecycleScope.launch {
           viewModel.allEventsFlow.collect { event ->
               when(event){
                   is MainViewModel.AllEvents.Message ->{
                       Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
                   }
               }
           }
        }
    }

    private fun registerObserver() {
        viewModel.currentUser.observe(viewLifecycleOwner,{ user ->
            user?.let {
                binding?.apply{
                    welcomeTxt.text = "welcome ${it.email}"
                    signinButton.text = "sign out"
                    signinButton.setOnClickListener {
                        viewModel.signOut()
                    }
                }
            }?: binding?.apply {
                welcomeTxt.isVisible = false
                signinButton.text = "sign in"
                signinButton.setOnClickListener {
                    findNavController().navigate(R.id.action_homeFragment_to_signInFragment)
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
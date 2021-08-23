package com.dev.james.firebasemvvm.ui.authentication

import android.os.Bundle
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
import com.dev.james.firebasemvvm.databinding.FragmentPasswordResetBinding
import com.dev.james.firebasemvvm.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ResetPasswordFragment : Fragment(R.layout.fragment_password_reset) {

    private var _binding : FragmentPasswordResetBinding? = null
    private val binding get() = _binding
    private val viewModel : MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPasswordResetBinding.inflate(inflater , container , false)
        setUpWidgets()
        listenToChannels()
        return binding?.root
    }

    private fun listenToChannels() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allEventsFlow.collect { event ->
                when(event){
                    is MainViewModel.AllEvents.Message -> {
                        Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_resetPasswordFragment_to_signInFragment)
                    }
                    is MainViewModel.AllEvents.Error -> {
                        binding?.apply {
                            resetPassProgressBar.isInvisible = true
                            errorText.text = event.error
                        }
                    }
                    is MainViewModel.AllEvents.ErrorCode -> {
                        if(event.code == 1)
                            binding?.apply {
                                userEmailEtvl.error = "email should not be empty!"
                                resetPassProgressBar.isInvisible = true
                            }
                    }
                }

            }
        }
    }

    private fun setUpWidgets() {
        binding?.apply {
            buttonResendPassword.setOnClickListener {
                resetPassProgressBar.isVisible = true
                val email = userEmailEtv.text.toString()
                viewModel.verifySendPasswordReset(email)
            }
        }
    }
}
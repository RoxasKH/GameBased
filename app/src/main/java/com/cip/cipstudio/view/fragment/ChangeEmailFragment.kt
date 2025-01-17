package com.cip.cipstudio.view.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.cip.cipstudio.R
import com.cip.cipstudio.databinding.FragmentEmailChangeBinding
import com.cip.cipstudio.model.User
import com.cip.cipstudio.utils.AuthTypeErrorEnum
import com.cip.cipstudio.viewmodel.ChangeEmailViewModel
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso

class ChangeEmailFragment : Fragment() {
    private val TAG = "ChangeEmailFragment"

    private lateinit var changeEmailViewModel: ChangeEmailViewModel
    private lateinit var changeEmailBinding: FragmentEmailChangeBinding
    private val user = User

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        changeEmailBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_email_change, container, false)
        changeEmailViewModel = ChangeEmailViewModel(changeEmailBinding)
        changeEmailBinding.changeEmailViewModel = changeEmailViewModel
        changeEmailBinding.executePendingBindings()


        changeEmailBinding.fEmailChangeBtnBack.backButton.setOnClickListener {
            findNavController().popBackStack()
        }


        initializeChangeEmailButton()

        return changeEmailBinding.root
    }

    private fun initializeChangeEmailButton() {
        changeEmailBinding.fEmailChangeBtnChange.setOnClickListener {
            changeEmailBinding.fEmailChangeLayoutPwd.error = ""
            changeEmailBinding.fEmailChangeLayoutEmail.error = ""
            changeEmailViewModel.changeEmail(
                onSuccess = {
                    Toast.makeText(requireContext(), "Email changed", Toast.LENGTH_SHORT).show()
                    findNavController(requireView()).navigate(R.id.action_changeEmailFragment_to_userFragment)
                },
                onFailure = {
                    when(it.getErrorType()){
                        AuthTypeErrorEnum.EMAIL -> {
                            changeEmailBinding.fEmailChangeLayoutEmail.error = getString(it.getErrorId())
                        }
                        AuthTypeErrorEnum.PASSWORD -> {
                            changeEmailBinding.fEmailChangeLayoutPwd.error = getString(it.getErrorId())
                        }
                        AuthTypeErrorEnum.LOGIN -> {
                            Toast.makeText(requireContext(), getString(it.getErrorId()), Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            Toast.makeText(requireContext(), it.getErrorId(), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        }
    }
}
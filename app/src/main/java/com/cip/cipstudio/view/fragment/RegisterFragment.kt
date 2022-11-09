package com.cip.cipstudio.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.cip.cipstudio.R
import com.cip.cipstudio.databinding.FragmentRegisterBinding
import com.cip.cipstudio.utils.AuthErrorEnum
import com.cip.cipstudio.utils.AuthTypeErrorEnum
import com.cip.cipstudio.viewmodel.LoginViewModel
import com.cip.cipstudio.viewmodel.RegisterViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout


class RegisterFragment : Fragment() {

    private lateinit var registerBinding : FragmentRegisterBinding
    private lateinit var registerViewModel: RegisterViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        registerBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_register, container, false)
        registerViewModel = RegisterViewModel(requireContext())
        registerBinding.registerViewModel = registerViewModel
        registerBinding.executePendingBindings()

        registerBinding.fRegisterTvSwitchMode.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }

        initializeRegisterButton()
        return registerBinding.root
    }

    private fun initializeRegisterButton(){
        registerBinding.fRegisterBtnRegister.setOnClickListener {
            registerBinding.fRegisterLayoutEmail.error = ""
            registerBinding.fRegisterLayoutPwd.error = ""
            registerBinding.fRegisterLayoutPwdConfirm.error = ""
            registerViewModel
                .register(onSuccess = {
                    findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                },
                    onFailure = {
                        when(it.getErrorType()){
                            AuthTypeErrorEnum.EMAIL -> {
                                registerBinding.fRegisterLayoutEmail.error = it.getErrorMessage(this.requireContext())
                            }
                            AuthTypeErrorEnum.PASSWORD -> {
                                registerBinding.fRegisterLayoutPwd.error = it.getErrorMessage(this.requireContext())
                            }
                            AuthTypeErrorEnum.CONFIRM_PASSWORD -> {
                                registerBinding.fRegisterLayoutPwdConfirm.error = it.getErrorMessage(this.requireContext())
                            }
                            AuthTypeErrorEnum.UNKNOWN -> {
                                Toast.makeText(context, it.getErrorMessage(this.requireContext()), Toast.LENGTH_SHORT).show()
                            }
                        }
                    })
        }
    }
}
package com.example.myapplication.frags

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.NavOptions
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.loginButton.setOnClickListener {
            loginUser()
        }
        binding.registerText.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }
    }
    private fun loginUser() {
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "მეილი და პაროლი აუცილებელია", Toast.LENGTH_SHORT).show()
            return
        }
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (_binding == null) {
                    Log.w("LoginFragment", "Binding is null, view destroyed after login attempt.")
                    return@addOnCompleteListener
                }
                if (task.isSuccessful) {
                    Log.d("LoginFragment", "signInWithEmailAndPassword:success")
                    val user = auth.currentUser
                    Toast.makeText(requireContext(), "ავტორიზირებულია!", Toast.LENGTH_SHORT).show()
                    val navOptions = NavOptions.Builder()
                        .setPopUpTo(R.id.nav_graph, true)
                        .build()
                    findNavController().navigate(R.id.navigation_home, null, navOptions)

                } else {
                    Log.w("LoginFragment", "signInWithEmailAndPassword:failure", task.exception)
                    Toast.makeText(requireContext(), "ავტორიზაცია ვერ მოხერხდა! ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("LoginFragment", "onDestroyView called, setting _binding to null.")
        _binding = null
    }
}
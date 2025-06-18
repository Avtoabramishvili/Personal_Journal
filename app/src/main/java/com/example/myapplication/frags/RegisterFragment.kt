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
import com.example.myapplication.databinding.FragmentRegisterBinding
import com.google.firebase.auth.FirebaseAuth

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.registerButton.setOnClickListener {
            registerUser()
        }
        binding.loginTextView.setOnClickListener {
            findNavController().navigate(R.id.action_register_to_login)
        }
    }
    private fun registerUser() {
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()
        val confirmPassword = binding.confirmPasswordEditText.text.toString().trim()
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(requireContext(), "ყველა ველი უნდა იყოს შევსებული", Toast.LENGTH_SHORT).show()
            return
        }
        if (password != confirmPassword) {
            Toast.makeText(requireContext(), "პაროლები არ ემთხვევა", Toast.LENGTH_SHORT).show()
            return
        }
        binding.progressBar.visibility = View.VISIBLE

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (_binding == null) {
                    Log.w("RegisterFragment", "Binding is null, view destroyed after registration attempt.")
                    return@addOnCompleteListener
                }
                binding.progressBar.visibility = View.GONE
                if (task.isSuccessful) {
                    Log.d("RegisterFragment", "createUserWithEmailAndPassword:success")
                    val user = auth.currentUser
                    Toast.makeText(requireContext(), "რეგისტრაცია დასრულდა წარმატებით!", Toast.LENGTH_SHORT).show()
                    val navOptions = NavOptions.Builder()
                        .setPopUpTo(R.id.nav_graph, true)
                        .build()
                    findNavController().navigate(R.id.navigation_home, null, navOptions)
                } else {
                    Log.w("RegisterFragment", "createUserWithEmailAndPassword:failure", task.exception)
                    Toast.makeText(requireContext(), "რეგისტრაცია ვერ მოხერხდა: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("RegisterFragment", "onDestroyView called, setting _binding to null.")
        _binding = null
    }
}

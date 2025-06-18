package com.example.myapplication.frags
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.NavOptions
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class ProfileFragment : Fragment() {
    private var _binding: com.example.myapplication.databinding.FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = result.data?.data
            imageUri?.let {
                uploadProfilePicture(it)
            }
        }
    }
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                openImageChooser()
            } else {
                Toast.makeText(requireContext(), "გალერეაზე წვდომის უფლება არაა", Toast.LENGTH_LONG).show()
            }
        }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = com.example.myapplication.databinding.FragmentProfileBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar: Toolbar = binding.profileToolbar
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.title = "პროფილი"
        loadUserProfile()
        setupListeners()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("ProfileFragment", "onDestroyView called, setting _binding to null.")
        _binding = null
    }
    private fun setupListeners() {
        if (_binding == null) {
            Log.w("ProfileFragment", "setupListeners: _binding is null. Skipping listener setup.")
            return
        }
        binding.profileImageView.setOnClickListener {
            checkPermissionsAndOpenChooser()
        }
        binding.saveProfileButton.setOnClickListener {
            saveProfileDetails()
        }
        binding.signOutButton.setOnClickListener {
            auth.signOut()
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, true)
                .build()
            findNavController().navigate(R.id.navigation_login, null, navOptions)
        }
    }
    private fun loadUserProfile() {
        val user = auth.currentUser
        user?.let { firebaseUser ->
            if (_binding == null) {
                Log.w("ProfileFragment", "loadUserProfile: _binding is null, fragment view already destroyed. Skipping UI update.")
                return
            }
            binding.emailTextView.text = firebaseUser.email ?: "No email"
            val userRef = database.getReference("users").child(firebaseUser.uid)
            userRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (_binding == null) {
                        Log.w("ProfileFragment", "onDataChange: _binding is null, fragment view already destroyed. Skipping UI update.")
                        return
                    }
                    val userName = snapshot.child("name").getValue(String::class.java)
                    val userLastName = snapshot.child("lastName").getValue(String::class.java)
                    val profilePicUrl = snapshot.child("profilePicUrl").getValue(String::class.java)
                    binding.nameEditText.setText(userName ?: "")
                    binding.lastNameEditText.setText(userLastName ?: "")
                    binding.nameTextView.text = if (!userName.isNullOrEmpty()) {
                        userName
                    } else {
                        "No name set"
                    }
                    profilePicUrl?.let { url ->
                        if (url.isNotEmpty()) {
                            Glide.with(this@ProfileFragment)
                                .load(url)
                                .placeholder(R.drawable.ic_profile_placeholder)
                                .error(R.drawable.ic_profile_placeholder)
                                .into(binding.profileImageView)
                        } else {
                            binding.profileImageView.setImageResource(R.drawable.ic_profile_placeholder)
                        }
                    } ?: run {
                        binding.profileImageView.setImageResource(R.drawable.ic_profile_placeholder)
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    if (_binding == null) {
                        Log.w("ProfileFragment", "onCancelled: _binding is null, fragment view already destroyed. Skipping UI update.")
                        return
                    }
                    Toast.makeText(requireContext(), "Failed to load user data: ${error.message}", Toast.LENGTH_SHORT).show()
                    Log.e("ProfileFragment", "Firebase DB ხარვეზი: ${error.message}")
                }
            })
        } ?: run {
            if (_binding == null) {
                Log.w("ProfileFragment", "loadUserProfile (not logged in): _binding is null. Skipping UI update.")
                return
            }
            binding.emailTextView.text = "არაა ავტორიზირებული"
            binding.nameEditText.setText("")
            binding.lastNameEditText.setText("")
            binding.nameTextView.text = "არაა სახელი"
            binding.profileImageView.setImageResource(R.drawable.ic_profile_placeholder)
        }
    }
    private fun saveProfileDetails() {
        if (_binding == null) {
            Log.w("ProfileFragment", "saveProfileDetails: _binding is null. Skipping operation.")
            Toast.makeText(requireContext(), "ვერ ინახება პროფილი", Toast.LENGTH_SHORT).show()
            return
        }
        val user = auth.currentUser
        user?.let { firebaseUser ->
            val name = binding.nameEditText.text.toString().trim()
            val lastName = binding.lastNameEditText.text.toString().trim()
            if (name.isEmpty() && lastName.isEmpty()) {
                Toast.makeText(requireContext(), "სახელი / გვარი არ შეიძლება იყოს ცარიელი", Toast.LENGTH_SHORT).show()
                return
            }

            val userRef = database.getReference("users").child(firebaseUser.uid)
            val updates = hashMapOf<String, Any>(
                "name" to name,
                "lastName" to lastName
            )
            userRef.updateChildren(updates)
                .addOnSuccessListener {
                    if (_binding == null) {
                        Log.w("ProfileFragment", "saveProfileDetails success: _binding is null. Skipping Toast.")
                        return@addOnSuccessListener
                    }
                    Toast.makeText(requireContext(), "პროფილი განახლებულია", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    if (_binding == null) {
                        Log.w("ProfileFragment", "saveProfileDetails failure: _binding is null. Skipping Toast/Log.")
                        return@addOnFailureListener
                    }
                    Toast.makeText(requireContext(), "Failed to save profile details: ${e.message}", Toast.LENGTH_LONG).show()
                    Log.e("ProfileFragment", "ვერ განახლდა პროფილის ფოტო ${e.message}")
                }
        } ?: run {
            Toast.makeText(requireContext(), "მომხმარებელი არაა ავტორიზირებული", Toast.LENGTH_SHORT).show()
        }
    }
    private fun checkPermissionsAndOpenChooser() {
        if (_binding == null) {
            Log.w("ProfileFragment", "checkPermissionsAndOpenChooser: _binding is null. Skipping operation.")
            Toast.makeText(requireContext(), "ვერ ხერხდება ფოტოს არჩევა", Toast.LENGTH_SHORT).show()
            return
        }
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        when {
            ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED -> {
                openImageChooser()
            }
            shouldShowRequestPermissionRationale(permission) -> {
                Toast.makeText(requireContext(), "მეხსიერებაზე წვდომა აუცილებელია, რათა აარჩიოთ პროფილის ფოტო", Toast.LENGTH_LONG).show()
                requestPermissionLauncher.launch(permission)
            }
            else -> {
                requestPermissionLauncher.launch(permission)
            }
        }
    }
    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }
    private fun uploadProfilePicture(imageUri: Uri) {
        if (_binding == null) {
            Log.w("ProfileFragment", "uploadProfilePicture: _binding is null. Skipping upload.")
            Toast.makeText(requireContext(), "ფოტოს ატვირთვა შეუძლებელია", Toast.LENGTH_SHORT).show()
            return
        }
        val user = auth.currentUser
        user?.let { firebaseUser ->
            binding.profilePicProgressBar.visibility = View.VISIBLE
            binding.profileImageView.alpha = 0.5f
            val storageRef = storage.reference
            val profilePicsRef = storageRef.child("profile_pictures/${firebaseUser.uid}.jpg")
            profilePicsRef.putFile(imageUri)
                .addOnSuccessListener { taskSnapshot ->
                    profilePicsRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        val userDbRef = database.getReference("users").child(firebaseUser.uid)
                        userDbRef.child("profilePicUrl").setValue(downloadUri.toString())
                            .addOnSuccessListener {
                                if (_binding == null) {
                                    Log.w("ProfileFragment", "Upload success save URL: _binding is null. Skipping Toast.")
                                    return@addOnSuccessListener
                                }
                                Toast.makeText(requireContext(), "პროფილის ფოტო განახლებულია", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                if (_binding == null) {
                                    Log.w("ProfileFragment", "Upload success save URL failure: _binding is null. Skipping Toast/Log.")
                                    return@addOnFailureListener
                                }
                                Toast.makeText(requireContext(), "ვერ მოხერხდა URL შენახვა ${e.message}", Toast.LENGTH_LONG).show()
                                Log.e("ProfileFragment", "ვერ მოხერხდა პროფილის ფოტოს URL მიღება ${e.message}")
                            }
                    }.addOnFailureListener { e ->
                        if (_binding == null) {
                            Log.w("ProfileFragment", "Get download URL failure: _binding is null. Skipping Toast/Log.")
                            return@addOnFailureListener
                        }
                        Toast.makeText(requireContext(), "ვერ მოხერხდა URL მიღება ${e.message}", Toast.LENGTH_LONG).show()
                        Log.e("ProfileFragment", "ვერ მოხერხდა URL მიღება ${e.message}")
                    }
                }
                .addOnFailureListener { e ->
                    if (_binding == null) {
                        Log.w("ProfileFragment", "Upload failure: _binding is null. Skipping Toast/Log.")
                        return@addOnFailureListener
                    }
                    Toast.makeText(requireContext(), "ატვირთვა ვერ მოხერხდა ${e.message}", Toast.LENGTH_LONG).show()
                    Log.e("ProfileFragment", "პროფილის ფოტოს ატვირთვა ვერ მოხერხდა ${e.message}")
                }
                .addOnCompleteListener {
                    if (_binding == null) {
                        Log.w("ProfileFragment", "Upload complete: _binding is null. Skipping UI visibility.")
                        return@addOnCompleteListener
                    }
                    binding.profilePicProgressBar.visibility = View.GONE
                    binding.profileImageView.alpha = 1.0f
                }
        } ?: run {
            Toast.makeText(requireContext(), "არაა მომხმარებელი ავტორიზირებული", Toast.LENGTH_SHORT).show()
        }
    }
}
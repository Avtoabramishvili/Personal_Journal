package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.navigation.NavOptions
import com.example.myapplication.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var navController: NavController
    private var isNavigationSetup = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate called.")
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        navController = navHostFragment.navController
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                Log.d("MainActivity", "AuthStateListener: User is signed in: ${user.email}")
                if (!isNavigationSetup) {
                    setupMainNavigationGraph()
                    isNavigationSetup = true
                    Log.d("MainActivity", "AuthStateListener: Main navigation graph setup.")
                }
                if (navController.currentDestination?.id == R.id.navigation_login ||
                    navController.currentDestination?.id == R.id.navigation_register) {
                    val navOptions = NavOptions.Builder()
                        .setPopUpTo(R.id.nav_graph, true)
                        .build()
                    navController.navigate(R.id.navigation_home, null, navOptions)
                    Log.d("MainActivity", "AuthStateListener: Navigated from auth screen to Home.")
                }
            } else {
                Log.d("MainActivity", "AuthStateListener: User is signed OUT.")
                isNavigationSetup = false
                val navOptions = NavOptions.Builder()
                    .setPopUpTo(R.id.nav_graph, true)
                    .build()
                navController.navigate(R.id.navigation_login, null, navOptions)
                Log.d("MainActivity", "AuthStateListener: Navigated to Login.")
            }
        }
        navController.addOnDestinationChangedListener { _, destination, _ ->
            Log.d("MainActivity", "Destination Changed Listener: ${destination.label} (${destination.id})")
            when (destination.id) {
                R.id.navigation_login, R.id.navigation_register -> {
                    binding.navView.visibility = View.GONE
                    Log.d("MainActivity", "Nav bar HIDDEN for auth screen.")
                }
                else -> {
                    if (auth.currentUser != null) {
                        binding.navView.visibility = View.VISIBLE
                        Log.d("MainActivity", "Nav bar SHOWN for authenticated user on main screen.")
                    } else {
                        binding.navView.visibility = View.GONE
                        Log.d("MainActivity", "Nav bar HIDDEN for unauthenticated user on non-auth screen (error case).")
                    }
                }
            }
        }
        if (auth.currentUser == null) {
            Log.d("MainActivity", "onCreate: User is initially null, navigating to Login.")
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, true)
                .build()
            navController.navigate(R.id.navigation_login, null, navOptions)
        } else {
            Log.d("MainActivity", "onCreate: User is initially NOT null, setting up main graph.")
            setupMainNavigationGraph()
            isNavigationSetup = true
            if (navController.currentDestination?.id == R.id.navigation_login ||
                navController.currentDestination?.id == R.id.navigation_register) {
                val navOptions = NavOptions.Builder()
                    .setPopUpTo(R.id.nav_graph, true)
                    .build()
                navController.navigate(R.id.navigation_home, null, navOptions)
                Log.d("MainActivity", "onCreate: Authenticated user forced to Home from auth screen.")
            }
        }
    }
    private fun setupMainNavigationGraph() {
        val navView: BottomNavigationView = binding.navView
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_add_entry,
                R.id.navigation_profile
            )
        )
        navView.setupWithNavController(navController)
        Log.d("MainActivity", "setupMainNavigationGraph: Main UI components configured.")
    }
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
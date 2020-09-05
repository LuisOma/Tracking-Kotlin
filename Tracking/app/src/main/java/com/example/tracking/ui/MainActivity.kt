package com.example.tracking.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import com.example.tracking.R
import com.example.tracking.databinding.ActivityMainBinding
import com.example.tracking.db.UserInfo
import com.example.tracking.utils.Constants.ACTION_FINISH_TRACK
import com.example.tracking.utils.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.example.tracking.ui.fragments.TrackingFragmentDirections
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    @Inject
    lateinit var userInfo: UserInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.rootView)
        setSupportActionBar(binding.toolbar)

        navController = findNavController(R.id.navHostFragment)

        navigateToTrackingFragmentIfNeeded(intent)

        with(binding) {
            bottomNavigationView.apply {
                setupWithNavController(navController)
                setOnNavigationItemReselectedListener { /* no-op */ }
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNavigationView.visibility = when (destination.id) {
                R.id.tracksFragmentList, R.id.trackingFragment -> View.VISIBLE
                else -> View.GONE
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToTrackingFragmentIfNeeded(intent)
    }

    private fun navigateToTrackingFragmentIfNeeded(intent: Intent?) {
        when (intent?.action) {
            ACTION_SHOW_TRACKING_FRAGMENT -> {
                navController.navigate(R.id.action_global_trackingFragment)
            }
            ACTION_FINISH_TRACK -> {
                val action = TrackingFragmentDirections.actionGlobalTrackingFragment(true)
                navController.navigate(action)
            }
        }
    }
}

package com.example.smartalert.ui.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.smartalert.R
import com.example.smartalert.ui.perfil.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView

    private val mapaFragment = MapaFragment()
    private val feedFragment = FeedFragment()
    private val contributeFragment = ContributeFragment()
    private val profileFragment = ProfileFragment()

    private var activeFragment: Fragment = mapaFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        bottomNav = findViewById(R.id.bottomNavigation)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, profileFragment, "PROFILE")
                .hide(profileFragment)
                .add(R.id.fragment_container, contributeFragment, "CONTRIBUTE")
                .hide(contributeFragment)
                .add(R.id.fragment_container, feedFragment, "FEED")
                .hide(feedFragment)
                .add(R.id.fragment_container, mapaFragment, "MAPA")
                .commit()

            activeFragment = mapaFragment
            bottomNav.selectedItemId = R.id.nav_map
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.nav_map -> {
                    showFragment(mapaFragment)
                    true
                }

                R.id.nav_feed -> {
                    showFragment(feedFragment)
                    true
                }

                R.id.nav_contribute -> {
                    showFragment(contributeFragment)
                    true
                }

                R.id.nav_you -> {
                    showFragment(profileFragment)
                    true
                }

                else -> false
            }
        }
    }

    private fun showFragment(fragment: Fragment) {
        if (fragment == activeFragment) return

        supportFragmentManager.beginTransaction()
            .hide(activeFragment)
            .show(fragment)
            .commit()

        activeFragment = fragment
    }

    // ✅ Volver al mapa SIN recrearlo
    fun mostrarMapa() {
        showFragment(mapaFragment)
        bottomNav.selectedItemId = R.id.nav_map
    }
}

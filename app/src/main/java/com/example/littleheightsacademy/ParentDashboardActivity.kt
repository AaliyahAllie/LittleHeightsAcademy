package com.example.littleheightsacademy

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class ParentDashboardActivity : AppCompatActivity() {

    private lateinit var btnRegisterStudent: LinearLayout
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parent_dashboard)

        auth = FirebaseAuth.getInstance()
        btnRegisterStudent = findViewById(R.id.btnRegisterStudent)
        bottomNav = findViewById(R.id.bottomNavigation)

        // Placeholder: navigate to RegisterStudentActivity (to be implemented later)
        btnRegisterStudent.setOnClickListener {
            Toast.makeText(this, "Register Student feature coming soon!", Toast.LENGTH_SHORT).show()
        }

        // Handle bottom navigation clicks
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Open Home screen (if not already there)
                    if (this !is ParentDashboardActivity) {
                        startActivity(Intent(this, ParentDashboardActivity::class.java))
                        overridePendingTransition(0, 0)
                        finish()
                    }
                    true
                }
                R.id.nav_profile -> {
                    // Open ParentProfileActivity (or student profile)
                    if (this !is ParentProfileActivity) {
                        startActivity(Intent(this, ParentProfileActivity::class.java))
                        overridePendingTransition(0, 0)
                        finish()
                    }
                    true
                }
                R.id.nav_menu -> {
                    // Open MenuActivity (or implement logout separately)
                    if (this !is NavigationActivity) {
                        startActivity(Intent(this, NavigationActivity::class.java))
                        overridePendingTransition(0, 0)
                        finish()
                    }
                    true
                }
                else -> false
            }
        }

    }
}

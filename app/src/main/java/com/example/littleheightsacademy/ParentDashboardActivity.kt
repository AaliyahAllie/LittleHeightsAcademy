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
                    Toast.makeText(this, "Already on Home", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_students -> {
                    Toast.makeText(this, "Students feature coming soon!", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_settings -> {
                    // Simple example: Logout for now
                    auth.signOut()
                    Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, ParentLoginActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }
}

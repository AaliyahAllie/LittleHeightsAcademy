package com.example.littleheightsacademy

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.auth.FirebaseAuth

class ParentDashboardActivity : AppCompatActivity() {

    private lateinit var btnRegisterStudent: LinearLayout
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var auth: FirebaseAuth

    // Progress views
    private lateinit var progressSeating: CircularProgressIndicator
    private lateinit var txtPercentage: TextView
    private lateinit var txtSeatsLeft: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parent_dashboard)

        auth = FirebaseAuth.getInstance()
        btnRegisterStudent = findViewById(R.id.btnRegisterStudent)
        bottomNav = findViewById(R.id.bottomNavigation)

        // Bind progress bar views
        progressSeating = findViewById(R.id.progressSeating)
        txtPercentage = findViewById(R.id.txtPercentage)
        txtSeatsLeft = findViewById(R.id.txtSeatsLeft)

        // Load seating info from Firebase
        loadSeatingProgress()

        // Navigate to RegisterStudentActivity on click
        btnRegisterStudent.setOnClickListener {
            startActivity(Intent(this, RegisterStudentActivity::class.java))
        }

        // Handle bottom navigation clicks
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    if (this !is ParentDashboardActivity) {
                        startActivity(Intent(this, ParentDashboardActivity::class.java))
                        overridePendingTransition(0, 0)
                        finish()
                    }
                    true
                }
                R.id.nav_profile -> {
                    if (this !is ParentProfileActivity) {
                        startActivity(Intent(this, ParentProfileActivity::class.java))
                        overridePendingTransition(0, 0)
                        finish()
                    }
                    true
                }
                R.id.nav_menu -> {
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

    private fun loadSeatingProgress() {
        val database = FirebaseDatabase.getInstance().reference.child("seating")

        database.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val capacityA = snapshot.child("A/capacity").getValue(Int::class.java) ?: 0
                val availableA = snapshot.child("A/available").getValue(Int::class.java) ?: 0
                val capacityB = snapshot.child("B/capacity").getValue(Int::class.java) ?: 0
                val availableB = snapshot.child("B/available").getValue(Int::class.java) ?: 0
                val capacityR = snapshot.child("R/capacity").getValue(Int::class.java) ?: 0
                val availableR = snapshot.child("R/available").getValue(Int::class.java) ?: 0

                val totalCapacity = capacityA + capacityB + capacityR
                val totalEnrolled = totalCapacity - (availableA + availableB + availableR)
                val percentageFilled = if (totalCapacity > 0) (totalEnrolled * 100) / totalCapacity else 0
                val seatsLeft = availableA + availableB + availableR

                // Update progress bar and texts
                progressSeating.progress = percentageFilled
                txtPercentage.text = "$percentageFilled%"
                txtSeatsLeft.text = "Seats left: $seatsLeft"
            }
        }
    }
}

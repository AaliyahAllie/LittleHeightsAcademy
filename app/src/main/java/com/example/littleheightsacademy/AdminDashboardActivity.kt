package com.example.littleheightsacademy

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.database.*

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var progressSeating: CircularProgressIndicator
    private lateinit var txtPercentage: TextView
    private lateinit var txtSeatsLeft: TextView

    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        // --- Bind views ---
        progressSeating = findViewById(R.id.progressSeating)
        txtPercentage = findViewById(R.id.txtPercentage)
        txtSeatsLeft = findViewById(R.id.txtSeatsLeft)

        // --- Bottom Navigation ---
        findViewById<LinearLayout>(R.id.navUsers).setOnClickListener {
            startActivity(Intent(this, AdminEnrollmentVerificationActivity::class.java))
            finish()
        }

        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            startActivity(Intent(this, AdminDashboardActivity::class.java))
            finish()
        }

        findViewById<LinearLayout>(R.id.navMenu).setOnClickListener {
            startActivity(Intent(this, NavigationAdminActivity::class.java))
            finish()
        }

        // --- Firebase Realtime DB reference ---
        database = FirebaseDatabase.getInstance().reference.child("seating")

        loadSeatingData()
    }

    private fun loadSeatingData() {
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
                val seatsLeft = totalCapacity - totalEnrolled
                val percentageFilled = if (totalCapacity > 0) (totalEnrolled * 100) / totalCapacity else 0

                // --- Update UI ---
                progressSeating.progress = percentageFilled
                txtPercentage.text = "$percentageFilled%"
                txtSeatsLeft.text = "Seats left: $seatsLeft"
            }
        }
    }
}

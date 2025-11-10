package com.example.littleheightsacademy

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth

class NavigationAdminActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation_admin) // link your XML file here

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // 🔹 Setup Toolbar (with back arrow)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            finish() // Go back to previous screen
        }

        // 🔹 Link Buttons
        val btnDashboard: Button = findViewById(R.id.btnDashboard)
        val btnEnrollmentHandler: Button = findViewById(R.id.btnEnrollmentHandler)
        val btnUpdateMarks: Button = findViewById(R.id.btnUpdateMarks)
        val btnUpdateSeats: Button = findViewById(R.id.btnUpdateSeats)
        val btnLogout: Button = findViewById(R.id.btnLogout)
        val btnInvoices: Button = findViewById(R.id.btnInvoices)

        // 🔹 Button Navigation
        btnDashboard.setOnClickListener {
            startActivity(Intent(this, AdminDashboardActivity::class.java))
        }

        btnEnrollmentHandler.setOnClickListener {
            startActivity(Intent(this, AdminEnrollmentVerificationActivity::class.java))
        }

        btnUpdateMarks.setOnClickListener {
            startActivity(Intent(this, AdminStudentMarksActivity::class.java))
        }

        btnUpdateSeats.setOnClickListener {
            startActivity(Intent(this, AdminUpdateSeatsActivity::class.java))
        }
        btnInvoices.setOnClickListener {
            startActivity(Intent(this, AdminInvoiceActivity::class.java))
        }
        btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, AdminLoginActivity::class.java))
            finish()
        }
    }
}

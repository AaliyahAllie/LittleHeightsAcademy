package com.example.littleheightsacademy

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class ParentProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var ivBack: ImageView
    private lateinit var ivEditChildren: ImageView
    private lateinit var btnViewPayments: Button
    private lateinit var btnViewReports: Button
    private lateinit var btnRegisterStudent: Button
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parent_profile)

        auth = FirebaseAuth.getInstance()

        // Initialize views
        ivBack = findViewById(R.id.ivBack)
        ivEditChildren = findViewById(R.id.ivEditChildren)
        btnViewPayments = findViewById(R.id.btnViewPayments)
        btnViewReports = findViewById(R.id.btnViewReports)
        btnRegisterStudent = findViewById(R.id.btnRegisterStudent)
        bottomNav = findViewById(R.id.bottomNavigation)

        // Back button
        ivBack.setOnClickListener { finish() }

        // View payments
        btnViewPayments.setOnClickListener {
            startActivity(Intent(this, PaymentHistoryActivity::class.java))
        }

        // View student reports
        btnViewReports.setOnClickListener {
            startActivity(Intent(this, ParentViewReportsActivity::class.java))
        }

        // Register new student
        btnRegisterStudent.setOnClickListener {
            startActivity(Intent(this, RegisterStudentActivity::class.java))
        }

        // Edit children placeholder
        ivEditChildren.setOnClickListener {
            // TODO: Open EditChildrenActivity if implemented
        }

        // Bottom navigation
        bottomNav.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, ParentDashboardActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    // Already here
                    true
                }
                R.id.nav_menu -> {
                    startActivity(Intent(this, NavigationActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
}

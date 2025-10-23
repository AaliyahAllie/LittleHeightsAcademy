package com.example.littleheightsacademy
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class NavigationActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)

        auth = FirebaseAuth.getInstance()


        val btnDashboard: Button = findViewById(R.id.btnDashboard)
        val btnPaymentHistory: Button = findViewById(R.id.btnPaymentHistory)
        val btnRegisterStudent: Button = findViewById(R.id.btnRegisterStudent)
        val btnTrackStatus: Button = findViewById(R.id.btnTrackStatus)
        val btnProfile: Button = findViewById(R.id.btnProfile)
        val btnLogout: Button = findViewById(R.id.btnLogout)



        btnDashboard.setOnClickListener {
            startActivity(Intent(this, ParentDashboardActivity::class.java))
        }

        btnPaymentHistory.setOnClickListener {
            startActivity(Intent(this, PaymentHistoryActivity::class.java))
        }

        btnRegisterStudent.setOnClickListener {
            startActivity(Intent(this, RegisterStudentActivity::class.java))
        }

        btnTrackStatus.setOnClickListener {
            startActivity(Intent(this, TrackStatusActivity::class.java))
        }

        btnProfile.setOnClickListener {
            startActivity(Intent(this, ParentProfileActivity::class.java))
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, ParentLoginActivity::class.java))
            finish() // prevents user from going back
        }
    }
}

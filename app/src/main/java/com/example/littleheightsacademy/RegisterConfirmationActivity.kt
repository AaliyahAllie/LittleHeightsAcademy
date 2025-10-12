package com.example.littleheightsacademy

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class RegistrationConfirmationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration_confirmation)

        val studentName = intent.getStringExtra("studentName")
        val tvMessage = findViewById<TextView>(R.id.tvMessage)
        val btnTrack = findViewById<Button>(R.id.btnTrackStatus)

        tvMessage.text = "Student $studentName has been successfully registered!"

        btnTrack.setOnClickListener {
            startActivity(Intent(this, TrackStatusActivity::class.java))
        }
    }
}

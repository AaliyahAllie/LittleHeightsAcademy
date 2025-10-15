package com.example.littleheightsacademy

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.ArrayAdapter
import android.widget.Spinner


class AdminEnrollmentVerificationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_enrollment_verification)
        val spinner = findViewById<Spinner>(R.id.spinnerOptions)
        val options = arrayOf("Select an option", "All Applications", "Closest Area", "Student Account", "Update Marks")
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, options)
        }

}
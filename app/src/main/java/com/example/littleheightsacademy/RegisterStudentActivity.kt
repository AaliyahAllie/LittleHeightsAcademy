package com.example.littleheightsacademy

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.littleheightsacademy.DatabaseHelper
import java.util.*

class RegisterStudentActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_student)

        dbHelper = DatabaseHelper(this)

        val etDob = findViewById<EditText>(R.id.etDob)
        val btnRegister = findViewById<Button>(R.id.btnRegisterStudent)
        val btnUpload = findViewById<Button>(R.id.btnUploadDocs)

        // Date Picker for DOB
        etDob.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)
            val dpd = DatePickerDialog(this, { _, y, m, d ->
                etDob.setText("$d/${m + 1}/$y")
            }, year, month, day)
            dpd.show()
        }

        // Upload Button (Mock)
        btnUpload.setOnClickListener {
            Toast.makeText(this, "Upload functionality coming soon!", Toast.LENGTH_SHORT).show()
        }

        // Register Button
        btnRegister.setOnClickListener {
            val firstName = findViewById<EditText>(R.id.etChildFirstName).text.toString()
            val lastName = findViewById<EditText>(R.id.etChildLastName).text.toString()
            val address = findViewById<EditText>(R.id.etAddress).text.toString()
            val zip = findViewById<EditText>(R.id.etZipCode).text.toString()
            val email = findViewById<EditText>(R.id.etParentEmail).text.toString()
            val dob = etDob.text.toString()

            val activities = mutableListOf<String>()
            if (findViewById<CheckBox>(R.id.cbKarate).isChecked) activities.add("Karate")
            if (findViewById<CheckBox>(R.id.cbSoccer).isChecked) activities.add("Soccer")
            if (findViewById<CheckBox>(R.id.cbIslamicStudies).isChecked) activities.add("Islamic Studies")
            if (findViewById<CheckBox>(R.id.cbSwimming).isChecked) activities.add("Swimming")

            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Please fill in all required fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save data to database
            dbHelper.insertStudent(firstName, lastName, address, zip, email, dob, activities.joinToString(","))

            // Navigate to Confirmation Screen
            val intent = Intent(this, RegistrationConfirmationActivity::class.java)
            intent.putExtra("studentName", "$firstName $lastName")
            startActivity(intent)
        }
    }
}

package com.example.littleheightsacademy

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.text.SimpleDateFormat
import java.util.*

class RegisterStudentActivity : AppCompatActivity() {

    private var documentUri: Uri? = null
    private val PICK_FILE_REQUEST = 123
    private lateinit var storageRef: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_student)

        storageRef = FirebaseStorage.getInstance().reference

        val etDob = findViewById<EditText>(R.id.etDob)
        val btnRegister = findViewById<Button>(R.id.btnRegisterStudent)
        val btnUpload = findViewById<Button>(R.id.btnUploadDocs)

        // Date Picker
        etDob.setOnClickListener {
            val c = Calendar.getInstance()
            val dpd = DatePickerDialog(
                this,
                { _, y, m, d -> etDob.setText("$d/${m + 1}/$y") },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
            )
            dpd.show()
        }

        // Upload Document (optional)
        btnUpload.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            startActivityForResult(Intent.createChooser(intent, "Select Document"), PICK_FILE_REQUEST)
        }

        // Register Student
        btnRegister.setOnClickListener {
            val firstName = findViewById<EditText>(R.id.etChildFirstName).text.toString().trim()
            val lastName = findViewById<EditText>(R.id.etChildLastName).text.toString().trim()
            val address = findViewById<EditText>(R.id.etAddress).text.toString().trim()
            val zip = findViewById<EditText>(R.id.etZipCode).text.toString().trim()
            val email = findViewById<EditText>(R.id.etParentEmail).text.toString().trim()
            val dobStr = etDob.text.toString().trim()

            val activities = mutableListOf<String>()
            if (findViewById<CheckBox>(R.id.cbKarate).isChecked) activities.add("Karate")
            if (findViewById<CheckBox>(R.id.cbSoccer).isChecked) activities.add("Soccer")
            if (findViewById<CheckBox>(R.id.cbIslamicStudies).isChecked) activities.add("Islamic Studies")
            if (findViewById<CheckBox>(R.id.cbSwimming).isChecked) activities.add("Swimming")

            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || dobStr.isEmpty()) {
                Toast.makeText(this, "Please fill in all required fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validate DOB
            val sdf = SimpleDateFormat("d/M/yyyy", Locale.US)
            val dob: Date
            try {
                dob = sdf.parse(dobStr)!!
            } catch (e: Exception) {
                Toast.makeText(this, "Invalid date format.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val today = Calendar.getInstance()
            val dobCalendar = Calendar.getInstance().apply { time = dob }

            // Check future date
            if (dobCalendar.after(today)) {
                Toast.makeText(this, "Date of birth cannot be in the future.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check minimum age (1 year)
            today.add(Calendar.YEAR, -1)
            if (dobCalendar.after(today)) {
                Toast.makeText(this, "Student must be at least 1 year old.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Call save method (optional document)
            saveStudent(firstName, lastName, address, zip, email, dobStr, activities)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_FILE_REQUEST && resultCode == Activity.RESULT_OK && data?.data != null) {
            documentUri = data.data
            Toast.makeText(this, "Document selected: ${documentUri?.lastPathSegment}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveStudent(
        firstName: String,
        lastName: String,
        address: String,
        zip: String,
        email: String,
        dob: String,
        activities: List<String>
    ) {
        val studentsRef = FirebaseDatabase.getInstance().getReference("students")
        val studentId = studentsRef.push().key ?: return

        if (documentUri != null) {
            val fileRef = storageRef.child("student_docs/$studentId-${documentUri?.lastPathSegment}")
            fileRef.putFile(documentUri!!)
                .addOnSuccessListener {
                    fileRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        saveStudentData(studentsRef, studentId, firstName, lastName, address, zip, email, dob, activities, downloadUrl.toString())
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to upload document: ${e.message}", Toast.LENGTH_SHORT).show()
                    saveStudentData(studentsRef, studentId, firstName, lastName, address, zip, email, dob, activities, null)
                }
        } else {
            saveStudentData(studentsRef, studentId, firstName, lastName, address, zip, email, dob, activities, null)
        }
    }

    private fun saveStudentData(
        ref: DatabaseReference,
        studentId: String,
        firstName: String,
        lastName: String,
        address: String,
        zip: String,
        email: String,
        dob: String,
        activities: List<String>,
        documentUrl: String?
    ) {
        val studentData = mutableMapOf<String, Any>(
            "id" to studentId,
            "firstName" to firstName,
            "lastName" to lastName,
            "address" to address,
            "zip" to zip,
            "email" to email,
            "dob" to dob,
            "activities" to activities,
            "status" to "PENDING"
        )

        documentUrl?.let { studentData["documentUrl"] = it }

        ref.child(studentId)
            .setValue(studentData)
            .addOnSuccessListener {
                Toast.makeText(this, "Student registered successfully!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, RegistrationConfirmationActivity::class.java)
                intent.putExtra("studentName", "$firstName $lastName")
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save student: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

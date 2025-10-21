package com.example.littleheightsacademy

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*

class RegisterStudentActivity : AppCompatActivity() {

    private var documentUri: Uri? = null
    private val PICK_FILE_REQUEST = 123
    private lateinit var storageRef: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_student)

        // Initialize Firebase Storage reference
        storageRef = FirebaseStorage.getInstance().reference

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

        // Upload Button
        btnUpload.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*" // allow any file type
            startActivityForResult(Intent.createChooser(intent, "Select Document"), PICK_FILE_REQUEST)
        }

        // Register Button
        btnRegister.setOnClickListener {
            val firstName = findViewById<EditText>(R.id.etChildFirstName).text.toString().trim()
            val lastName = findViewById<EditText>(R.id.etChildLastName).text.toString().trim()
            val address = findViewById<EditText>(R.id.etAddress).text.toString().trim()
            val zip = findViewById<EditText>(R.id.etZipCode).text.toString().trim()
            val email = findViewById<EditText>(R.id.etParentEmail).text.toString().trim()
            val dob = etDob.text.toString().trim()

            val activities = mutableListOf<String>()
            if (findViewById<CheckBox>(R.id.cbKarate).isChecked) activities.add("Karate")
            if (findViewById<CheckBox>(R.id.cbSoccer).isChecked) activities.add("Soccer")
            if (findViewById<CheckBox>(R.id.cbIslamicStudies).isChecked) activities.add("Islamic Studies")
            if (findViewById<CheckBox>(R.id.cbSwimming).isChecked) activities.add("Swimming")

            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Please fill in all required fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (documentUri == null) {
                Toast.makeText(this, "Please upload a document.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            uploadDocumentAndSaveStudent(firstName, lastName, address, zip, email, dob, activities)
        }
    }

    // Handle file selection result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_FILE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            documentUri = data.data
            Toast.makeText(this, "Document selected: ${documentUri?.lastPathSegment}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadDocumentAndSaveStudent(
        firstName: String,
        lastName: String,
        address: String,
        zip: String,
        email: String,
        dob: String,
        activities: List<String>
    ) {
        val studentsRef = FirebaseDatabase.getInstance().getReference("students")
        val studentId: String = studentsRef.push().key ?: return

        // Upload document to Firebase Storage
        val fileRef: StorageReference = storageRef.child("student_docs/$studentId-${documentUri?.lastPathSegment}")

        documentUri?.let { uri: Uri ->
            fileRef.putFile(uri)
                .addOnSuccessListener { taskSnapshot ->
                    fileRef.downloadUrl.addOnSuccessListener { downloadUrl: Uri ->
                        // Save student data including document URL
                        val studentData: Map<String, Any> = mapOf(
                            "id" to studentId,
                            "firstName" to firstName,
                            "lastName" to lastName,
                            "address" to address,
                            "zip" to zip,
                            "email" to email,
                            "dob" to dob,
                            "activities" to activities,
                            "documentUrl" to downloadUrl.toString(),
                            "status" to "PENDING"
                        )

                        studentsRef.child(studentId)
                            .setValue(studentData)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Student registered successfully!", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, RegistrationConfirmationActivity::class.java)
                                intent.putExtra("studentName", "$firstName $lastName")
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e: Exception ->
                                Toast.makeText(this, "Failed to save student: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener { e: Exception ->
                    Toast.makeText(this, "Failed to upload document: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}

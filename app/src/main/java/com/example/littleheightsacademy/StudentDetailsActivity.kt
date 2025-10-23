package com.example.littleheightsacademy

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class StudentDetailsActivity : AppCompatActivity() {

    private lateinit var databaseRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_details)

        // Initialize UI elements
        val tvName = findViewById<TextView>(R.id.tvStudentName)
        val tvEmail = findViewById<TextView>(R.id.tvStudentEmail)
        val tvDob = findViewById<TextView>(R.id.tvStudentDob)
        val tvAddress = findViewById<TextView>(R.id.tvStudentAddress)
        val tvZip = findViewById<TextView>(R.id.tvStudentZip)
        val tvActivities = findViewById<TextView>(R.id.tvStudentActivities)
        val tvStatus = findViewById<TextView>(R.id.tvStudentStatus)
        val btnOpenDoc = findViewById<Button>(R.id.btnViewDocument)

        // Get the studentId passed from previous screen
        val studentId = intent.getStringExtra("studentId")
        if (studentId == null) {
            Toast.makeText(this, "No student selected.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Reference to Firebase
        databaseRef = FirebaseDatabase.getInstance().getReference("students").child(studentId)

        // Fetch student details
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val firstName = snapshot.child("firstName").getValue(String::class.java) ?: ""
                    val lastName = snapshot.child("lastName").getValue(String::class.java) ?: ""
                    val email = snapshot.child("email").getValue(String::class.java) ?: ""
                    val dob = snapshot.child("dob").getValue(String::class.java) ?: ""
                    val address = snapshot.child("address").getValue(String::class.java) ?: ""
                    val zip = snapshot.child("zip").getValue(String::class.java) ?: ""
                    val status = snapshot.child("status").getValue(String::class.java) ?: ""
                    val documentUrl = snapshot.child("documentUrl").getValue(String::class.java) ?: ""
                    val activitiesList = snapshot.child("activities").children.mapNotNull {
                        it.getValue(String::class.java)
                    }

                    // Display student data
                    tvName.text = "$firstName $lastName"
                    tvEmail.text = "Email: $email"
                    tvDob.text = "Date of Birth: $dob"
                    tvAddress.text = "Address: $address"
                    tvZip.text = "ZIP Code: $zip"
                    tvStatus.text = "Status: $status"
                    tvActivities.text = "Activities: ${activitiesList.joinToString(", ")}"

                    // Handle document button
                    btnOpenDoc.setOnClickListener {
                        if (documentUrl.isNotEmpty()) {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(documentUrl))
                            startActivity(intent)
                        } else {
                            Toast.makeText(this@StudentDetailsActivity, "No document available.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this@StudentDetailsActivity, "Student record not found.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@StudentDetailsActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

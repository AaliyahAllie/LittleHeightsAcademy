package com.example.littleheightsacademy

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class AdminEnrollmentVerificationActivity : AppCompatActivity() {

    private lateinit var spinnerOptions: Spinner
    private lateinit var btnExecuteOption: Button
    private lateinit var btnViewAllApplications: Button
    private lateinit var btnViewClosestArea: Button
    private lateinit var btnViewStudentAccount: Button
    private lateinit var btnUpdateMarks: Button

    private lateinit var studentsRef: DatabaseReference
    private lateinit var pendingStudentsList: MutableList<Student>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_enrollment_verification)

        // Firebase reference
        studentsRef = FirebaseDatabase.getInstance().getReference("students")

        // Initialize views
        spinnerOptions = findViewById(R.id.spinnerOptions)
        btnExecuteOption = findViewById(R.id.btnExecuteOption)
        btnViewAllApplications = findViewById(R.id.btnViewAllApplications)
        btnViewClosestArea = findViewById(R.id.btnViewClosestArea)
        btnViewStudentAccount = findViewById(R.id.btnViewStudentAccount)
        btnUpdateMarks = findViewById(R.id.btnUpdateMarks)

        // Load spinner options
        val options = arrayOf("Pending Applications", "Approved Applications", "Rejected Applications")
        spinnerOptions.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, options)

        // Load Pending Students by default
        loadPendingStudents()

        // Buttons for various admin actions
        btnViewAllApplications.setOnClickListener {
            // Open the new ViewAllApplicationsActivity
            val intent = Intent(this, ViewAllApplicationsActivity::class.java)
            startActivity(intent)
        }

        btnViewClosestArea.setOnClickListener {
            Toast.makeText(this, "Feature coming soon: Filter by area!", Toast.LENGTH_SHORT).show()
        }

        btnViewStudentAccount.setOnClickListener {
            startActivity(Intent(this, AdminStudentAccountActivity::class.java))
        }

        btnUpdateMarks.setOnClickListener {
            startActivity(Intent(this, AdminStudentMarksActivity::class.java))
        }

        btnExecuteOption.setOnClickListener {
            val selected = spinnerOptions.selectedItem.toString()
            when (selected) {
                "Pending Applications" -> loadPendingStudents()
                "Approved Applications" -> loadByStatus("APPROVED")
                "Rejected Applications" -> loadByStatus("REJECTED")
            }
        }

        // Bottom navigation
        setupBottomNavigation()
    }

    private fun loadPendingStudents() {
        pendingStudentsList = mutableListOf()
        studentsRef.orderByChild("status").equalTo("PENDING")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    pendingStudentsList.clear()
                    for (studentSnap in snapshot.children) {
                        val student = studentSnap.getValue(Student::class.java)
                        student?.let { pendingStudentsList.add(it) }
                    }
                    if (pendingStudentsList.isEmpty()) {
                        Toast.makeText(this@AdminEnrollmentVerificationActivity, "No pending applications found.", Toast.LENGTH_SHORT).show()
                    } else {
                        showStudentsDialog("Pending Applications", pendingStudentsList)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@AdminEnrollmentVerificationActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun loadByStatus(status: String) {
        studentsRef.orderByChild("status").equalTo(status)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<Student>()
                    for (studentSnap in snapshot.children) {
                        val student = studentSnap.getValue(Student::class.java)
                        student?.let { list.add(it) }
                    }
                    if (list.isEmpty()) {
                        Toast.makeText(this@AdminEnrollmentVerificationActivity, "No $status applications found.", Toast.LENGTH_SHORT).show()
                    } else {
                        showStudentsDialog("$status Applications", list)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@AdminEnrollmentVerificationActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    // Pop-up dialog showing student info
    private fun showStudentsDialog(title: String, students: List<Student>) {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle(title)

        val studentNames = students.map { "${it.firstName} ${it.lastName} - ${it.status}" }.toTypedArray()
        builder.setItems(studentNames) { _, which ->
            val selected = students[which]
            val intent = Intent(this, StudentDetailsActivity::class.java)
            intent.putExtra("studentId", selected.id)
            startActivity(intent)
        }

        builder.setNegativeButton("Close", null)
        builder.show()
    }

    // Bottom navigation logic
    private fun setupBottomNavigation() {
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            startActivity(Intent(this, AdminDashboardActivity::class.java))
            finish()
        }

        findViewById<LinearLayout>(R.id.navUsers).setOnClickListener {
            Toast.makeText(this, "User management coming soon!", Toast.LENGTH_SHORT).show()
        }

        findViewById<LinearLayout>(R.id.navMenu).setOnClickListener {
            startActivity(Intent(this, NavigationAdminActivity::class.java))
            finish()
        }
    }


}

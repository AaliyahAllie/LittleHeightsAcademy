package com.example.littleheightsacademy

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.*

class AdminViewStudentAccountActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var studentTable: TableLayout
    private lateinit var searchInput: EditText
    private lateinit var searchButton: Button
    private lateinit var availableSeatsText: TextView

    private var allStudents: MutableList<Student> = mutableListOf()
    private val totalSeats = 150 // You can update this dynamically if needed

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_view_student_account)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Firebase setup
        database = FirebaseDatabase.getInstance().getReference("students")

        // Initialize UI components
        studentTable = findViewById(R.id.studentTable)
        searchInput = findViewById(R.id.searchInput)
        searchButton = findViewById(R.id.searchButton)
        availableSeatsText = findViewById(R.id.availableSeats)

        fetchStudents()

        // Search functionality
        searchButton.setOnClickListener {
            val query = searchInput.text.toString().trim()
            if (query.isEmpty()) {
                updateTable(allStudents)
            } else {
                val filtered = allStudents.filter {
                    "${it.firstName} ${it.lastName}".contains(query, ignoreCase = true)
                }
                updateTable(filtered)
            }
        }

        // Bottom navigation
        setupBottomNavigation()
    }

    private fun fetchStudents() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allStudents.clear()
                for (child in snapshot.children) {
                    val student = child.getValue(Student::class.java)
                    student?.let { allStudents.add(it) }
                }

                // Show only approved students
                val approvedStudents = allStudents.filter { it.status == "APPROVED" }

                updateTable(approvedStudents)
                updateAvailableSeats()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AdminViewStudentAccountActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateAvailableSeats() {
        val approvedCount = allStudents.count { it.status == "APPROVED" }
        val remainingSeats = totalSeats - approvedCount
        availableSeatsText.text = remainingSeats.toString()
    }

    private fun updateTable(students: List<Student>) {
        studentTable.removeAllViews()

        // Table Header
        val headerRow = TableRow(this)
        val headers = arrayOf("Student Name", "Status", "Action")
        headers.forEach { title ->
            val tv = TextView(this)
            tv.text = title
            tv.setPadding(8, 8, 8, 8)
            tv.textSize = 14f
            tv.setBackgroundColor(0xFFECECEC.toInt())
            headerRow.addView(tv)
        }
        studentTable.addView(headerRow)

        // Student Rows
        for (student in students) {
            val row = TableRow(this)
            row.gravity = Gravity.CENTER_VERTICAL

            val name = TextView(this)
            name.text = "${student.firstName} ${student.lastName}"
            name.setPadding(8, 8, 8, 8)
            row.addView(name)

            val status = TextView(this)
            status.text = student.status ?: "PENDING"
            status.setPadding(8, 8, 8, 8)
            when (student.status) {
                "APPROVED" -> {
                    status.setTextColor(0xFF4CAF50.toInt()) // Green
                    status.setBackgroundColor(0xFFE8F5E9.toInt())
                }
                "REJECTED" -> {
                    status.setTextColor(0xFFF44336.toInt()) // Red
                    status.setBackgroundColor(0xFFFFEBEE.toInt())
                }
                else -> {
                    status.setTextColor(0xFFFFC107.toInt()) // Amber
                    status.setBackgroundColor(0xFFFFF8E1.toInt())
                }
            }
            row.addView(status)

            val action = TextView(this)
            action.text = "View"
            action.setTextColor(0xFF3F51B5.toInt())
            action.setPadding(8, 8, 8, 8)
            action.setOnClickListener { showStudentDetailsDialog(student) }
            row.addView(action)

            studentTable.addView(row)
        }
    }

    private fun showStudentDetailsDialog(student: Student) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("${student.firstName} ${student.lastName}")

        val message = """
            First Name: ${student.firstName}
            Last Name: ${student.lastName}
            Email: ${student.email}
            Address: ${student.address}
            ZIP: ${student.zip}
            DOB: ${student.dob}
            Status: ${student.status ?: "PENDING"}
            Document: ${student.documentUrl ?: "Not uploaded"}
        """.trimIndent()

        builder.setMessage(message)
        builder.setPositiveButton("Open Document") { _, _ ->
            student.documentUrl?.let {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it)))
            } ?: Toast.makeText(this, "No document uploaded", Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton("Close", null)
        builder.show()
    }

    private fun setupBottomNavigation() {
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            startActivity(Intent(this, AdminDashboardActivity::class.java))
            finish()
        }

        findViewById<LinearLayout>(R.id.navUsers).setOnClickListener {
            startActivity(Intent(this, AdminEnrollmentVerificationActivity::class.java))
            finish()
        }

        findViewById<LinearLayout>(R.id.navMenu).setOnClickListener {
            startActivity(Intent(this, NavigationAdminActivity::class.java))
            finish()
        }
    }
}
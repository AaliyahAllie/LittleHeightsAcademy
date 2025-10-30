package com.example.littleheightsacademy

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import java.util.*

class ViewAllApplicationsActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var studentTable: TableLayout
    private lateinit var searchNameEdit: EditText
    private lateinit var searchButton: Button
    private lateinit var dateInput: EditText
    private lateinit var filterButton: Button
    private lateinit var tabApproved: Button
    private lateinit var tabPending: Button
    private lateinit var tabRejected: Button

    private var allStudents: MutableList<Student> = mutableListOf()
    private var currentFilterStatus: String? = null
    private var selectedYear: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_all_applications)

        // Initialize views
        database = FirebaseDatabase.getInstance().getReference("students")
        studentTable = findViewById(R.id.studentTable)
        searchNameEdit = findViewById(R.id.searchStudent)
        searchButton = findViewById(R.id.searchButton)
        dateInput = findViewById(R.id.dateInput)
        filterButton = findViewById(R.id.filterButton)
        tabApproved = findViewById(R.id.tabApproved)
        tabPending = findViewById(R.id.tabPending)
        tabRejected = findViewById(R.id.tabRejected)

        fetchAllStudents()

        // Search by name
        searchButton.setOnClickListener { applyFilters() }

        // Date picker for year selection
        dateInput.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val dpd = DatePickerDialog(this, { _, y, _, _ ->
                selectedYear = y.toString()
                dateInput.setText(selectedYear)
            }, year, 0, 1)
            dpd.datePicker.maxDate = c.timeInMillis
            dpd.show()
        }
        filterButton.setOnClickListener { applyFilters() }

        // Status tabs
        tabApproved.setOnClickListener { currentFilterStatus = "APPROVED"; applyFilters() }
        tabPending.setOnClickListener { currentFilterStatus = "PENDING"; applyFilters() }
        tabRejected.setOnClickListener { currentFilterStatus = "REJECTED"; applyFilters() }

        //Bottom Navigation
        setupBottomNavigation()
    }

    private fun fetchAllStudents() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allStudents.clear()
                for (child in snapshot.children) {
                    val student = child.getValue(Student::class.java)
                    student?.let { allStudents.add(it) }
                }
                applyFilters()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ViewAllApplicationsActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun applyFilters() {
        val filteredList = allStudents.filter { student ->
            val matchesStatus = currentFilterStatus?.let { student.status == it } ?: true
            val matchesName = searchNameEdit.text.toString().trim().let {
                it.isEmpty() || "${student.firstName} ${student.lastName}".contains(it, ignoreCase = true)
            }
            val matchesYear = selectedYear?.let { student.dob?.endsWith(it) ?: false } ?: true
            matchesStatus && matchesName && matchesYear
        }
        updateTable(filteredList)
    }

    private fun updateTable(students: List<Student>) {
        studentTable.removeAllViews()
        // Add header
        val header = TableRow(this)
        val headers = arrayOf("Student", "Doc", "Status", "Date", "Action")
        headers.forEach { h ->
            val tv = TextView(this)
            tv.text = h
            tv.setPadding(4, 4, 4, 4)
            tv.setBackgroundColor(0xFFECECEC.toInt())
            tv.textSize = 12f
            header.addView(tv)
        }
        studentTable.addView(header)

        // Add student rows
        students.forEach { addStudentRow(it) }
    }

    private fun addStudentRow(student: Student) {
        val row = TableRow(this)
        row.setPadding(4, 4, 4, 4)
        row.gravity = Gravity.CENTER_VERTICAL

        val nameText = TextView(this)
        nameText.text = "${student.firstName} ${student.lastName}"
        row.addView(nameText)

        val viewBtn = Button(this)
        viewBtn.text = "View"
        viewBtn.setOnClickListener { showStudentDetailsDialog(student) }
        row.addView(viewBtn)

        val statusText = TextView(this)
        statusText.text = student.status ?: "PENDING"
        row.addView(statusText)

        val dobText = TextView(this)
        dobText.text = student.dob ?: "-"
        row.addView(dobText)

        val actionBtn = Button(this)
        actionBtn.text = "Action"
        actionBtn.setOnClickListener { showStatusUpdateDialog(student, statusText) }
        row.addView(actionBtn)

        studentTable.addView(row)
    }

    private fun showStudentDetailsDialog(student: Student) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("${student.firstName} ${student.lastName}")

        val message = StringBuilder()
        message.append("First Name: ${student.firstName}\n")
        message.append("Last Name: ${student.lastName}\n")
        message.append("Email: ${student.email}\n")
        message.append("Address: ${student.address}\n")
        message.append("ZIP: ${student.zip}\n")
        message.append("DOB: ${student.dob}\n")
        message.append("Activities: ${student.activities?.joinToString(", ") ?: "-"}\n")
        message.append("Status: ${student.status ?: "PENDING"}\n")
        message.append("Document: ${student.documentUrl ?: "Not uploaded"}")

        builder.setMessage(message.toString())
        builder.setPositiveButton("Open Document") { _, _ ->
            student.documentUrl?.let { url ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            } ?: Toast.makeText(this, "No document uploaded", Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton("Close", null)
        builder.show()
    }

    private fun showStatusUpdateDialog(student: Student, statusTextView: TextView) {
        val options = arrayOf("APPROVED", "REJECTED")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Update Status for ${student.firstName}")
        builder.setItems(options) { _, which ->
            val newStatus = options[which]
            database.child(student.id ?: return@setItems).child("status").setValue(newStatus)
                .addOnSuccessListener {
                    statusTextView.text = newStatus
                    Toast.makeText(this, "${student.firstName} status updated to $newStatus", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to update status: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
        builder.show()
    }
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

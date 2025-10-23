package com.example.littleheightsacademy

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*

class TrackStatusActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var studentContainer: LinearLayout
    private lateinit var searchNameEdit: EditText
    private lateinit var searchButton: Button
    private lateinit var dateInput: EditText
    private lateinit var filterButton: Button

    private var allStudents: MutableList<Student> = mutableListOf()
    private var selectedYear: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_track_status)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("students")

        studentContainer = findViewById(R.id.studentContainer)

        setupListeners()
        loadStudentsForParent()
    }

    private fun setupListeners() {
        // Search
        searchButton.setOnClickListener { applyFilters() }

        // Date picker
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
    }

    private fun loadStudentsForParent() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show()
            return
        }
        val parentEmail = currentUser.email ?: return

        database.orderByChild("email").equalTo(parentEmail)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    allStudents.clear()
                    for (child in snapshot.children) {
                        val student = child.getValue(Student::class.java)
                        student?.let { allStudents.add(it) }
                    }
                    applyFilters()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@TrackStatusActivity, "Database error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun applyFilters() {
        val filtered = allStudents.filter { student ->
            val matchesName = searchNameEdit.text.toString().trim().let {
                it.isEmpty() || "${student.firstName} ${student.lastName}".contains(it, ignoreCase = true)
            }
            val matchesYear = selectedYear?.let { student.dob?.endsWith(it) ?: false } ?: true
            matchesName && matchesYear
        }
        displayStudents(filtered)
    }

    private fun displayStudents(students: List<Student>) {
        studentContainer.removeAllViews()
        if (students.isEmpty()) {
            val tv = TextView(this).apply {
                text = "No students found."
                textSize = 16f
                setTextColor(resources.getColor(android.R.color.white))
            }
            studentContainer.addView(tv)
            return
        }

        students.forEach { student ->
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(24, 24, 24, 24)
                background = getDrawable(R.drawable.student_card_bg)
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 16, 0, 0)
                layoutParams = params
            }

            val nameView = TextView(this).apply {
                text = "${student.firstName} ${student.lastName}"
                textSize = 20f
                setTextColor(resources.getColor(android.R.color.black))
                setPadding(0, 0, 0, 8)
            }

            val statusView = TextView(this).apply {
                text = "Status: ${student.status ?: "PENDING"}"
                textSize = 16f
                setTextColor(
                    when (student.status) {
                        "APPROVED" -> resources.getColor(android.R.color.holo_green_dark)
                        "REJECTED" -> resources.getColor(android.R.color.holo_red_dark)
                        else -> resources.getColor(android.R.color.darker_gray)
                    }
                )
                setPadding(0, 0, 0, 8)
            }

            val viewButton = Button(this).apply {
                text = "View Details"
                setOnClickListener { showStudentDetailsDialog(student) }
            }

            card.addView(nameView)
            card.addView(statusView)
            card.addView(viewButton)

            studentContainer.addView(card)
        }
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

}

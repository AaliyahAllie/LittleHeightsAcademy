package com.example.littleheightsacademy

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class TrackStatusActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var studentContainer: LinearLayout
    private lateinit var searchNameEdit: EditText
    private lateinit var searchButton: Button

    private var allStudents: MutableList<Student> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_track_status)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("students")

        // Initialize views
        studentContainer = findViewById(R.id.studentContainer)

        // Dynamically add search bar and button at top
        searchNameEdit = EditText(this).apply {
            hint = "Search by name"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        searchButton = Button(this).apply {
            text = "Search"
        }

        val topLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            addView(searchNameEdit, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            addView(searchButton)
        }
        studentContainer.addView(topLayout, 0)

        // Button click listeners
        searchButton.setOnClickListener { applyFilters() }

        // Load students under this parent
        loadStudentsForParent()
    }

    private fun loadStudentsForParent() {
        val currentUser = auth.currentUser ?: return
        val parentEmail = currentUser.email ?: return

        database.orderByChild("email").equalTo(parentEmail)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    allStudents.clear()
                    for (child in snapshot.children) {
                        val student = child.getValue(Student::class.java)
                        student?.let { allStudents.add(it) }
                    }
                    displayStudents(allStudents)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@TrackStatusActivity, "Database error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun applyFilters() {
        val query = searchNameEdit.text.toString().trim()
        val filtered = allStudents.filter {
            query.isEmpty() || "${it.firstName} ${it.lastName}".contains(query, ignoreCase = true)
        }
        displayStudents(filtered)
    }

    private fun displayStudents(students: List<Student>) {
        // Clear old views except search bar
        studentContainer.removeViews(1, studentContainer.childCount - 1)

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
            }

            val statusView = TextView(this).apply {
                text = "Status: ${student.status ?: "PENDING"}"
                textSize = 16f
                setTextColor(
                    when(student.status) {
                        "APPROVED" -> resources.getColor(android.R.color.holo_green_dark)
                        "REJECTED" -> resources.getColor(android.R.color.holo_red_dark)
                        else -> resources.getColor(android.R.color.darker_gray)
                    }
                )
            }

            val viewButton = Button(this).apply {
                text = "View Report"
                setOnClickListener { openStudentReport(student) }
            }

            card.addView(nameView)
            card.addView(statusView)
            card.addView(viewButton)

            studentContainer.addView(card)
        }
    }

    private fun openStudentReport(student: Student) {
        if (student.documentUrl.isNullOrEmpty()) {
            Toast.makeText(this, "No report uploaded for ${student.firstName}", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(student.documentUrl))
        startActivity(intent)
    }
}

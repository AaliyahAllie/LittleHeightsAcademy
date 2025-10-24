package com.example.littleheightsacademy

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class TrackStatusActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var studentContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_track_status)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("students")

        studentContainer = findViewById(R.id.studentContainer)

        // Load and listen for live changes
        loadStudentsForParent()
    }

    private fun loadStudentsForParent() {
        val currentUser = auth.currentUser ?: return
        val parentEmail = currentUser.email?.lowercase() ?: return

        // Get ALL students first, then filter in code (to handle case-insensitive match)
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val students = mutableListOf<Student>()

                for (child in snapshot.children) {
                    val student = child.getValue(Student::class.java)
                    if (student != null && student.email.lowercase() == parentEmail) {
                        students.add(student)
                    }
                }

                displayStudents(students)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@TrackStatusActivity,
                    "Database error: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun displayStudents(students: List<Student>) {
        studentContainer.removeAllViews()

        if (students.isEmpty()) {
            val tv = TextView(this).apply {
                text = "No students found under your account."
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
                text = "Registration Status: ${student.status ?: "PENDING"}"
                textSize = 16f
                setTextColor(
                    when (student.status) {
                        "APPROVED" -> resources.getColor(android.R.color.holo_green_dark)
                        "REJECTED" -> resources.getColor(android.R.color.holo_red_dark)
                        else -> resources.getColor(android.R.color.darker_gray)
                    }
                )
            }

            card.addView(nameView)
            card.addView(statusView)
            studentContainer.addView(card)
        }
    }
}

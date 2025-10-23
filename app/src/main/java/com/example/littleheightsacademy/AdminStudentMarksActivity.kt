package com.example.littleheightsacademy

import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class AdminStudentMarksActivity : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance().reference
    private lateinit var studentTable: TableLayout
    private lateinit var searchStudent: EditText
    private lateinit var btnSearch: Button

    private val approvedStudents = mutableListOf<StudentMarks>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_student_marks)

        studentTable = findViewById(R.id.studentTable)
        searchStudent = findViewById(R.id.searchStudent)
        btnSearch = findViewById(R.id.btnSearch)

        loadApprovedStudents()

        // Search button logic
        btnSearch.setOnClickListener {
            val query = searchStudent.text.toString().trim().lowercase()
            if (query.isNotEmpty()) {
                filterStudents(query)
            } else {
                displayStudents(approvedStudents)
            }
        }
    }

    private fun loadApprovedStudents() {
        database.child("students").get().addOnSuccessListener { snapshot ->
            approvedStudents.clear()

            snapshot.children.forEach { snap ->
                val student = snap.getValue(Student::class.java)
                if (student != null && student.status == "APPROVED") {
                    approvedStudents.add(StudentMarks(student))
                }
            }

            if (approvedStudents.isEmpty()) {
                Toast.makeText(this, "No approved students found", Toast.LENGTH_SHORT).show()
            }

            displayStudents(approvedStudents)
        }
    }

    private fun displayStudents(list: List<StudentMarks>) {
        studentTable.removeAllViews()
        addTableHeader()
        list.forEach { addStudentRow(it) }
    }

    private fun filterStudents(query: String) {
        val filtered = approvedStudents.filter {
            it.name.lowercase().contains(query)
        }
        displayStudents(filtered)
    }

    private fun addTableHeader() {
        val headerRow = TableRow(this)
        headerRow.setBackgroundColor(resources.getColor(android.R.color.darker_gray, theme))
        headerRow.setPadding(6, 6, 6, 6)

        val headers = listOf("Student", "Class Mark", "Islamic Studies", "Action")
        headers.forEach { text ->
            val tv = TextView(this)
            tv.text = text
            tv.gravity = Gravity.CENTER
            tv.setPadding(4, 4, 4, 4)
            tv.setTextColor(resources.getColor(android.R.color.white, theme))
            tv.textSize = 13f
            headerRow.addView(tv)
        }
        studentTable.addView(headerRow)
    }

    private fun addStudentRow(studentMarks: StudentMarks) {
        val row = TableRow(this)
        row.setPadding(4, 4, 4, 4)

        // Student Name
        val tvName = TextView(this)
        tvName.text = studentMarks.name
        tvName.gravity = Gravity.CENTER
        row.addView(tvName)

        // Class Mark EditText
        val etClassMark = EditText(this)
        etClassMark.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        etClassMark.gravity = Gravity.CENTER
        etClassMark.hint = studentMarks.classMark.toString()
        row.addView(etClassMark)

        // Islamic Studies EditText
        val etIslamicMark = EditText(this)
        etIslamicMark.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        etIslamicMark.gravity = Gravity.CENTER
        etIslamicMark.hint = studentMarks.islamicMark.toString()
        row.addView(etIslamicMark)

        // Upload Button
        val btnUpload = Button(this)
        btnUpload.text = "Upload"
        row.addView(btnUpload)

        btnUpload.setOnClickListener {
            val classMark = etClassMark.text.toString().toIntOrNull() ?: 0
            val islamicMark = etIslamicMark.text.toString().toIntOrNull() ?: 0
            studentMarks.classMark = classMark
            studentMarks.islamicMark = islamicMark

            saveReport(studentMarks)
        }

        studentTable.addView(row)
    }

    private fun saveReport(studentMarks: StudentMarks) {
        val reportData = mapOf(
            "classMark" to studentMarks.classMark,
            "islamicMark" to studentMarks.islamicMark,
            "studentName" to studentMarks.name,
            "studentId" to studentMarks.student.id
        )

        database.child("reports")
            .child(studentMarks.student.id)
            .setValue(reportData)
            .addOnSuccessListener {
                Toast.makeText(this, "${studentMarks.name} report uploaded", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to upload report", Toast.LENGTH_SHORT).show()
            }
    }
}



package com.example.littleheightsacademy

import android.content.ContentValues
import android.content.DialogInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import android.content.Intent

class AdminStudentMarksActivity : AppCompatActivity() {

    private lateinit var studentsRef: DatabaseReference
    private lateinit var reportsRef: DatabaseReference
    private lateinit var reportTable: TableLayout
    private lateinit var searchStudent: EditText
    private lateinit var btnSearch: Button

    private val allStudents = mutableListOf<Map<String, Any?>>()
    private val allReports = mutableMapOf<String, Map<String, Any?>>()

    private val handler = Handler(Looper.getMainLooper())
    private val refreshInterval = 5000L // 5 seconds

    // ✅ Added: track if user is editing
    private var isEditing = false
    private val editPauseHandler = Handler(Looper.getMainLooper())
    private val editPauseDelay = 3000L // 3 seconds after typing stops

    private val refreshRunnable = object : Runnable {
        override fun run() {
            if (!isEditing) { // ✅ Only refresh when not editing
                fetchAllStudentsAndReports()
            }
            handler.postDelayed(this, refreshInterval)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_generate_report)

        reportTable = findViewById(R.id.reportTable)
        searchStudent = findViewById(R.id.searchStudent)
        btnSearch = findViewById(R.id.btnSearch)

        studentsRef = FirebaseDatabase.getInstance().getReference("students")
        reportsRef = FirebaseDatabase.getInstance().getReference("reports")

        fetchAllStudentsAndReports()
        handler.postDelayed(refreshRunnable, refreshInterval)

        // 🔍 Search functionality
        btnSearch.setOnClickListener {
            val query = searchStudent.text.toString().trim().lowercase()
            val filtered = allStudents.filter {
                getStudentName(it).lowercase().contains(query)
            }
            updateTable(filtered)
        }

        setupBottomNavigation()
    }

    // 🔹 Fetch all students and their corresponding reports
    private fun fetchAllStudentsAndReports() {
        studentsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(studentSnapshot: DataSnapshot) {
                allStudents.clear()

                for (child in studentSnapshot.children) {
                    val studentData = child.value as? Map<String, Any?> ?: continue
                    val studentId = child.key ?: continue

                    val fullStudentData = studentData.toMutableMap()
                    fullStudentData["studentId"] = studentId

                    allStudents.add(fullStudentData)
                }

                fetchReportsThenUpdateTable()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AdminStudentMarksActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // 🔹 Fetch reports after students are loaded
    private fun fetchReportsThenUpdateTable() {
        reportsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(reportSnapshot: DataSnapshot) {
                allReports.clear()
                for (child in reportSnapshot.children) {
                    val reportData = child.value as? Map<String, Any?> ?: continue
                    allReports[child.key ?: ""] = reportData
                }

                updateTable(allStudents)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AdminStudentMarksActivity, "Error loading reports: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // 🔹 Helper function to get student name safely
    private fun getStudentName(student: Map<String, Any?>): String {
        return when {
            student["studentName"] != null -> student["studentName"].toString()
            student["name"] != null -> student["name"].toString()
            student["fullName"] != null -> student["fullName"].toString()
            student["firstName"] != null && student["lastName"] != null ->
                "${student["firstName"]} ${student["lastName"]}"
            student["firstName"] != null -> student["firstName"].toString()
            else -> "Unknown"
        }
    }

    private fun updateTable(students: List<Map<String, Any?>>) {
        reportTable.removeAllViews()

        // Table Header
        val headerRow = TableRow(this)
        val headers = listOf("Student", "Class Mark", "Islamic Studies", "Activities", "Action")
        headers.forEach {
            val tv = TextView(this)
            tv.text = it
            tv.setPadding(8, 8, 8, 8)
            tv.textSize = 14f
            tv.setBackgroundColor(0xFFECECEC.toInt())
            headerRow.addView(tv)
        }
        reportTable.addView(headerRow)

        for (student in students) {
            val studentId = student["studentId"]?.toString() ?: continue
            val studentName = getStudentName(student)
            val existingReport = allReports[studentId]

            val row = TableRow(this)
            row.gravity = android.view.Gravity.CENTER_VERTICAL

            val name = TextView(this)
            name.text = studentName
            name.setPadding(8, 8, 8, 8)
            row.addView(name)

            val classMark = EditText(this)
            classMark.setText(existingReport?.get("classMark")?.toString() ?: "0")
            classMark.inputType = android.text.InputType.TYPE_CLASS_NUMBER
            classMark.setPadding(8, 8, 8, 8)
            addTypingListener(classMark)
            row.addView(classMark)

            val islamicMark = EditText(this)
            islamicMark.setText(existingReport?.get("islamicMark")?.toString() ?: "0")
            islamicMark.inputType = android.text.InputType.TYPE_CLASS_NUMBER
            islamicMark.setPadding(8, 8, 8, 8)
            addTypingListener(islamicMark)
            row.addView(islamicMark)

            val activitiesMark = EditText(this)
            activitiesMark.setText(existingReport?.get("activitiesMark")?.toString() ?: "0")
            activitiesMark.inputType = android.text.InputType.TYPE_CLASS_NUMBER
            activitiesMark.setPadding(8, 8, 8, 8)
            addTypingListener(activitiesMark)
            row.addView(activitiesMark)

            val action = Button(this)
            action.text = "Generate"
            action.setBackgroundColor(0xFF3F51B5.toInt())
            action.setTextColor(0xFFFFFFFF.toInt())
            action.setOnClickListener {
                val updatedReport = mapOf(
                    "studentId" to studentId,
                    "studentName" to studentName,
                    "classMark" to classMark.text.toString(),
                    "islamicMark" to islamicMark.text.toString(),
                    "activitiesMark" to activitiesMark.text.toString()
                )

                saveReportToFirebase(updatedReport)
                showReportPopup(updatedReport)
            }
            row.addView(action)

            reportTable.addView(row)
        }
    }

    // ✅ New helper to detect typing
    private fun addTypingListener(editText: EditText) {
        editText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                isEditing = true
                editPauseHandler.removeCallbacksAndMessages(null)
                editPauseHandler.postDelayed({ isEditing = false }, editPauseDelay)
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun saveReportToFirebase(report: Map<String, Any?>) {
        val studentId = report["studentId"]?.toString() ?: return
        reportsRef.child(studentId).setValue(report)
            .addOnSuccessListener {
                Toast.makeText(this, "Report updated for ${report["studentName"]}", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error saving report: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showReportPopup(report: Map<String, Any?>) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Student Report")

        val message = """
            Student Name: ${report["studentName"]}
            Student ID: ${report["studentId"]}
            Class Mark: ${report["classMark"]}
            Islamic Studies Mark: ${report["islamicMark"]}
            Activities Mark: ${report["activitiesMark"]}
        """.trimIndent()

        builder.setMessage(message)
        builder.setPositiveButton("Download") { _: DialogInterface, _: Int ->
            saveReportToDevice(report)
        }
        builder.setNegativeButton("Close", null)
        builder.create().show()
    }

    private fun saveReportToDevice(report: Map<String, Any?>) {
        val fileName = "${report["studentName"]}_report.txt"
        val content = """
            Student Name: ${report["studentName"]}
            Student ID: ${report["studentId"]}
            Class Mark: ${report["classMark"]}
            Islamic Studies: ${report["islamicMark"]}
            Activities Mark: ${report["activitiesMark"]}
        """.trimIndent()

        try {
            val outputStream: OutputStream? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = contentResolver
                val contentValues = ContentValues().apply {
                    put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                    put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri: Uri? = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                uri?.let { resolver.openOutputStream(it) }
            } else {
                val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(path, fileName)
                FileOutputStream(file)
            }

            outputStream?.use {
                it.write(content.toByteArray())
                it.flush()
            }

            Toast.makeText(this, "Report saved to device: $fileName", Toast.LENGTH_LONG).show()

        } catch (e: Exception) {
            Toast.makeText(this, "Error saving file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(refreshRunnable)
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

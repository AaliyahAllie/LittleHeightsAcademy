package com.example.littleheightsacademy

import android.content.ContentValues
import android.content.DialogInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import android.content.Intent

class AdminStudentMarksActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var reportTable: TableLayout
    private lateinit var searchStudent: EditText
    private lateinit var btnSearch: Button
    private val allReports = mutableListOf<Map<String, Any?>>()
    private var reportListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_generate_report)

        reportTable = findViewById(R.id.reportTable)
        searchStudent = findViewById(R.id.searchStudent)
        btnSearch = findViewById(R.id.btnSearch)

        database = FirebaseDatabase.getInstance().getReference("reports")

        fetchReports()

        // Search functionality
        btnSearch.setOnClickListener {
            val query = searchStudent.text.toString().trim().lowercase()
            val filtered = allReports.filter {
                (it["studentName"] as? String)?.lowercase()?.contains(query) == true
            }
            updateTable(filtered)
        }

        setupBottomNavigation()
    }

    private fun fetchReports() {
        reportListener?.let { database.removeEventListener(it) }

        reportListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allReports.clear()
                for (child in snapshot.children) {
                    val data = child.value as? Map<String, Any?> ?: continue
                    allReports.add(data)
                }
                reportTable.removeAllViews()
                updateTable(allReports)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AdminStudentMarksActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }

        database.addValueEventListener(reportListener!!)
    }

    private fun updateTable(reports: List<Map<String, Any?>>) {
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

        for (report in reports) {
            val row = TableRow(this)
            row.gravity = android.view.Gravity.CENTER_VERTICAL

            val name = TextView(this)
            name.text = report["studentName"]?.toString() ?: "Unknown"
            name.setPadding(8, 8, 8, 8)
            row.addView(name)

            val classMark = EditText(this)
            classMark.setText(report["classMark"]?.toString() ?: "0")
            classMark.inputType = android.text.InputType.TYPE_CLASS_NUMBER
            classMark.setPadding(8, 8, 8, 8)
            row.addView(classMark)

            val islamicMark = EditText(this)
            islamicMark.setText(report["islamicMark"]?.toString() ?: "0")
            islamicMark.inputType = android.text.InputType.TYPE_CLASS_NUMBER
            islamicMark.setPadding(8, 8, 8, 8)
            row.addView(islamicMark)

            val activitiesMark = EditText(this)
            activitiesMark.setText(report["activitiesMark"]?.toString() ?: "0")
            activitiesMark.inputType = android.text.InputType.TYPE_CLASS_NUMBER
            activitiesMark.setPadding(8, 8, 8, 8)
            row.addView(activitiesMark)

            val action = Button(this)
            action.text = "Generate"
            action.setBackgroundColor(0xFF3F51B5.toInt())
            action.setTextColor(0xFFFFFFFF.toInt())
            action.setOnClickListener {
                val updatedReport = mapOf(
                    "studentId" to report["studentId"],
                    "studentName" to report["studentName"],
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

    private fun saveReportToFirebase(report: Map<String, Any?>) {
        val studentId = report["studentId"]?.toString() ?: return
        database.child(studentId).setValue(report)
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
        reportListener?.let { database.removeEventListener(it) }
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

package com.example.littleheightsacademy

import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import android.content.Intent

class AdminGenerateReportActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var reportTable: TableLayout
    private lateinit var searchStudent: EditText
    private lateinit var btnSearch: Button
    private val allReports = mutableListOf<Map<String, Any>>()
    private var reportListener: ValueEventListener? = null // track listener reference

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

        // Bottom navigation
        setupBottomNavigation()
    }

    private fun fetchReports() {
        // Ensure old listener is removed first (prevents duplicates)
        reportListener?.let { database.removeEventListener(it) }

        reportListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allReports.clear()

                for (child in snapshot.children) {
                    val data = child.value as? Map<String, Any> ?: continue
                    allReports.add(data)
                }

                // ✅ Clear the table before updating to prevent duplicates
                reportTable.removeAllViews()
                updateTable(allReports)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AdminGenerateReportActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }

        database.addValueEventListener(reportListener!!)
    }

    private fun updateTable(reports: List<Map<String, Any>>) {
        // Table Header
        val headerRow = TableRow(this)
        val headers = listOf("Student", "Class Mark", "Islamic Studies", "Action")
        headers.forEach {
            val tv = TextView(this)
            tv.text = it
            tv.setPadding(8, 8, 8, 8)
            tv.textSize = 14f
            tv.setBackgroundColor(0xFFECECEC.toInt())
            headerRow.addView(tv)
        }
        reportTable.addView(headerRow)

        // Table Rows
        for (report in reports) {
            val row = TableRow(this)
            row.gravity = Gravity.CENTER_VERTICAL

            val name = TextView(this)
            name.text = report["studentName"].toString()
            name.setPadding(8, 8, 8, 8)
            row.addView(name)

            val classMark = TextView(this)
            classMark.text = report["classMark"].toString()
            classMark.setPadding(8, 8, 8, 8)
            row.addView(classMark)

            val islamicMark = TextView(this)
            islamicMark.text = report["islamicMark"].toString()
            islamicMark.setPadding(8, 8, 8, 8)
            row.addView(islamicMark)

            val action = Button(this)
            action.text = "Generate"
            action.setBackgroundColor(0xFF3F51B5.toInt())
            action.setTextColor(0xFFFFFFFF.toInt())
            action.setOnClickListener {
                saveReportToFirebase(report)
                showReportPopup(report)
            }
            row.addView(action)

            reportTable.addView(row)
        }
    }

    private fun saveReportToFirebase(report: Map<String, Any>) {
        val studentId = report["studentId"]?.toString() ?: return
        val studentName = report["studentName"]?.toString() ?: "Unknown"
        val classMark = report["classMark"]?.toString() ?: "-"
        val islamicMark = report["islamicMark"]?.toString() ?: "-"

        val reportData = mapOf(
            "studentId" to studentId,
            "studentName" to studentName,
            "classMark" to classMark,
            "islamicMark" to islamicMark,
            "timestamp" to System.currentTimeMillis()
        )

        // ✅ Use child(studentId) to update the same student's report instead of push()
        database.child(studentId).setValue(reportData)
            .addOnSuccessListener {
                Toast.makeText(this, "Report updated for $studentName", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error saving report: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showReportPopup(report: Map<String, Any>) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Student Report")

        val name = report["studentName"]?.toString() ?: "Unknown"
        val classMark = report["classMark"]?.toString() ?: "-"
        val islamicMark = report["islamicMark"]?.toString() ?: "-"
        val studentId = report["studentId"]?.toString() ?: "N/A"

        val message = """
            Student Name: $name
            
            Student ID: $studentId
            
            Class Mark: $classMark
            
            Islamic Studies Mark: $islamicMark
        """.trimIndent()

        builder.setMessage(message)
        builder.setPositiveButton("Close", null)

        val dialog = builder.create()
        dialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        // ✅ Remove Firebase listener to prevent double-calls and duplication
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
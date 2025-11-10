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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_generate_report)

        reportTable = findViewById(R.id.reportTable)
        searchStudent = findViewById(R.id.searchStudent)
        btnSearch = findViewById(R.id.btnSearch)

        database = FirebaseDatabase.getInstance().getReference("reports")

        fetchReports()

        // Search function
        btnSearch.setOnClickListener {
            val query = searchStudent.text.toString().trim().lowercase()
            val filtered = allReports.filter {
                (it["studentName"] as? String)?.lowercase()?.contains(query) == true
            }
            updateTable(filtered)
        }

        //BottomNav
        setupBottomNavigation()

    }

    private fun fetchReports() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allReports.clear()
                for (child in snapshot.children) {
                    val data = child.value as? Map<String, Any> ?: continue
                    allReports.add(data)
                }
                updateTable(allReports)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AdminGenerateReportActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateTable(reports: List<Map<String, Any>>) {
        reportTable.removeAllViews()

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

        // Rows
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
                showReportPopup(report)
            }
            row.addView(action)

            reportTable.addView(row)
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

        // Optional: Add a small “Download” option
        builder.setNeutralButton("Download PDF") { _, _ ->
            Toast.makeText(this, "Downloading report for $name...", Toast.LENGTH_SHORT).show()
            // PDF generation logic can go here
        }

        val dialog = builder.create()
        dialog.show()
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
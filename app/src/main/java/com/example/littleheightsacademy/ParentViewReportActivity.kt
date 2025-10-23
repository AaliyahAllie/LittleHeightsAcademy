package com.example.littleheightsacademy

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ParentViewReportsActivity : AppCompatActivity() {

    private lateinit var reportTable: TableLayout
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private val parentEmail: String
        get() = auth.currentUser?.email ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parent_view_report)

        reportTable = findViewById(R.id.reportTable)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        loadReports()
    }

    private fun loadReports() {
        database.child("students").get().addOnSuccessListener { snapshot ->
            val studentIds = mutableListOf<String>()
            snapshot.children.forEach { snap ->
                val student = snap.getValue(Student::class.java)
                if (student != null && student.status == "APPROVED" && student.email == parentEmail) {
                    studentIds.add(student.id)
                }
            }

            if (studentIds.isEmpty()) {
                Toast.makeText(this, "No reports found for your children", Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }

            displayReports(studentIds)
        }
    }

    private fun displayReports(studentIds: List<String>) {
        reportTable.removeAllViews()
        addTableHeader()

        studentIds.forEach { studentId ->
            database.child("reports").child(studentId).get().addOnSuccessListener { snap ->
                if (snap.exists()) {
                    val studentName = snap.child("studentName").getValue(String::class.java) ?: "Unknown"
                    val classMark = snap.child("classMark").getValue(Int::class.java) ?: 0
                    val islamicMark = snap.child("islamicMark").getValue(Int::class.java) ?: 0

                    addReportRow(studentName, classMark, islamicMark)
                }
            }
        }
    }

    private fun addTableHeader() {
        val headerRow = TableRow(this)
        headerRow.setBackgroundColor(resources.getColor(android.R.color.darker_gray, theme))
        headerRow.setPadding(6, 6, 6, 6)

        val headers = listOf("Student", "Class Mark", "Islamic Studies", "Download")
        headers.forEach { text ->
            val tv = TextView(this)
            tv.text = text
            tv.gravity = Gravity.CENTER
            tv.setPadding(4, 4, 4, 4)
            tv.setTextColor(resources.getColor(android.R.color.white, theme))
            tv.textSize = 13f
            headerRow.addView(tv)
        }
        reportTable.addView(headerRow)
    }

    private fun addReportRow(studentName: String, classMark: Int, islamicMark: Int) {
        val row = TableRow(this)
        row.setPadding(4, 4, 4, 4)

        val tvName = TextView(this)
        tvName.text = studentName
        tvName.gravity = Gravity.CENTER
        row.addView(tvName)

        val tvClass = TextView(this)
        tvClass.text = classMark.toString()
        tvClass.gravity = Gravity.CENTER
        row.addView(tvClass)

        val tvIslamic = TextView(this)
        tvIslamic.text = islamicMark.toString()
        tvIslamic.gravity = Gravity.CENTER
        row.addView(tvIslamic)

        val btnDownload = Button(this)
        btnDownload.text = "Download"
        btnDownload.setOnClickListener {
            downloadReport(studentName, classMark, islamicMark)
        }
        row.addView(btnDownload)

        reportTable.addView(row)
    }

    private fun downloadReport(studentName: String, classMark: Int, islamicMark: Int) {
        // Generate a simple text file report (could be PDF if desired)
        val reportContent = """
            Student Name: $studentName
            Class Mark: $classMark
            Islamic Studies: $islamicMark
        """.trimIndent()

        val filename = "${studentName.replace(" ", "_")}_Report.txt"
        val fileUri = Uri.parse("file://${filesDir.absolutePath}/$filename")
        openFileOutput(filename, Context.MODE_PRIVATE).use { it.write(reportContent.toByteArray()) }

        Toast.makeText(this, "Report saved: $filename", Toast.LENGTH_LONG).show()

        // Optional: trigger DownloadManager to save to Downloads folder
        val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadUri = Uri.parse("file://${filesDir.absolutePath}/$filename")
        val request = DownloadManager.Request(downloadUri)
            .setTitle(filename)
            .setDescription("Student Report")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir("/Download", filename)

        dm.enqueue(request)
    }
}

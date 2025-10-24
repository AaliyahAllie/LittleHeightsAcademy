package com.example.littleheightsacademy

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ParentViewReportsActivity : AppCompatActivity() {

    private lateinit var reportTable: TableLayout
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private val handler = Handler(Looper.getMainLooper())

    private val parentEmail: String
        get() = auth.currentUser?.email ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parent_view_report)

        reportTable = findViewById(R.id.reportTable)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        startAutoRefresh()
    }

    private fun startAutoRefresh() {
        handler.post(object : Runnable {
            override fun run() {
                loadReports()
                handler.postDelayed(this, 5000) // refresh every 5 seconds
            }
        })
    }

    private fun loadReports() {
        // Step 1: Fetch students where parent email matches
        database.child("students").get().addOnSuccessListener { snapshot ->
            val studentsUnderParent = snapshot.children.mapNotNull { snap ->
                snap.getValue(Student::class.java)?.takeIf { it.email == parentEmail }
            }

            if (studentsUnderParent.isEmpty()) {
                reportTable.removeAllViews()
                val noData = TextView(this)
                noData.text = "No students found for your account."
                noData.gravity = Gravity.CENTER
                reportTable.addView(noData)
                return@addOnSuccessListener
            }

            // Step 2: Fetch marks for each student
            displayReports(studentsUnderParent)
        }
    }

    private fun displayReports(students: List<Student>) {
        reportTable.removeAllViews()
        addTableHeader()

        students.forEach { student ->
            val studentId = student.id
            database.child("reports").child(studentId).get().addOnSuccessListener { snap ->
                val classMark = snap.child("classMark").getValue(Int::class.java) ?: 0
                val islamicMark = snap.child("islamicMark").getValue(Int::class.java) ?: 0
                addReportRow("${student.firstName} ${student.lastName}", classMark, islamicMark)
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
        val reportContent = """
            Student Name: $studentName
            Class Mark: $classMark
            Islamic Studies: $islamicMark
        """.trimIndent()

        val filename = "${studentName.replace(" ", "_")}_Report.txt"
        openFileOutput(filename, Context.MODE_PRIVATE).use { it.write(reportContent.toByteArray()) }

        Toast.makeText(this, "Report saved: $filename", Toast.LENGTH_LONG).show()

        val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadUri = Uri.parse("file://${filesDir.absolutePath}/$filename")
        val request = DownloadManager.Request(downloadUri)
            .setTitle(filename)
            .setDescription("Student Report")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir("/Download", filename)

        dm.enqueue(request)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}

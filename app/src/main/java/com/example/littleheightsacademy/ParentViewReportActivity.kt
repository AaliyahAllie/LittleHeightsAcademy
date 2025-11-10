package com.example.littleheightsacademy

import android.content.ContentValues
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.io.OutputStream

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

        findViewById<ImageView>(R.id.ivBack).setOnClickListener { finish() }

        startAutoRefresh()
    }

    private fun startAutoRefresh() {
        handler.post(object : Runnable {
            override fun run() {
                loadReports()
                handler.postDelayed(this, 5000)
            }
        })
    }

    private fun loadReports() {
        reportTable.removeAllViews()
        addTableHeader()

        database.child("students").get()
            .addOnSuccessListener { snapshot ->
                val studentsUnderParent = snapshot.children.mapNotNull { snap ->
                    snap.getValue(Student::class.java)
                        ?.takeIf { it.email.equals(parentEmail, ignoreCase = true) }
                }

                if (studentsUnderParent.isEmpty()) {
                    val noData = TextView(this)
                    noData.text = "No students found for your account."
                    noData.gravity = Gravity.CENTER
                    reportTable.addView(noData)
                    return@addOnSuccessListener
                }

                studentsUnderParent.forEach { student ->
                    database.child("reports").child(student.id).get()
                        .addOnSuccessListener { reportSnap ->
                            val classMark =
                                reportSnap.child("classMark").getValue(String::class.java)
                                    ?.toIntOrNull() ?: 0
                            val islamicMark =
                                reportSnap.child("islamicMark").getValue(String::class.java)
                                    ?.toIntOrNull() ?: 0
                            val activities =
                                reportSnap.child("activities").getValue(String::class.java) ?: "-"

                            addReportRow(student, classMark, islamicMark, activities)
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                this,
                                "Failed to fetch report for ${student.firstName}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to fetch students.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addTableHeader() {
        val headerRow = TableRow(this)
        headerRow.setBackgroundColor(resources.getColor(android.R.color.darker_gray, theme))
        headerRow.setPadding(6, 6, 6, 6)

        val headers = listOf("Student", "Class Mark", "Islamic Studies", "Activities", "Download")
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

    private fun addReportRow(
        student: Student,
        classMark: Int,
        islamicMark: Int,
        activities: String
    ) {
        val row = TableRow(this)
        row.setPadding(4, 4, 4, 4)

        val tvName = TextView(this)
        tvName.text = "${student.firstName} ${student.lastName}"
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

        val tvActivities = TextView(this)
        tvActivities.text = activities
        tvActivities.gravity = Gravity.CENTER
        row.addView(tvActivities)

        val btnDownload = Button(this)
        btnDownload.text = "Download"
        btnDownload.setOnClickListener {
            generateReportFile(student, classMark, islamicMark, activities)
        }
        row.addView(btnDownload)

        reportTable.addView(row)
    }

    private fun generateReportFile(
        student: Student,
        classMark: Int,
        islamicMark: Int,
        activities: String
    ) {
        val reportContent = """
        Student Name: ${student.firstName} ${student.lastName}
        Class Mark: $classMark
        Islamic Studies: $islamicMark
        Activities: $activities
    """.trimIndent()

        val filename = "${student.firstName}_${student.lastName}_Report.txt"

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+
                val resolver = contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "Download/")
                }
                val uri =
                    resolver.insert(MediaStore.Downloads.getContentUri("external"), contentValues)
                uri?.let {
                    resolver.openOutputStream(it)
                        ?.use { out -> out.write(reportContent.toByteArray()) }
                    Toast.makeText(this, "Report saved to Downloads: $filename", Toast.LENGTH_LONG)
                        .show()
                } ?: run {
                    Toast.makeText(this, "Failed to save report.", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Android 6.0 - 9 (API 24-28)
                val downloads =
                    android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
                if (!downloads.exists()) downloads.mkdirs()
                val file = java.io.File(downloads, filename)
                file.writeText(reportContent)
                Toast.makeText(this, "Report saved to Downloads: $filename", Toast.LENGTH_LONG)
                    .show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error saving report: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
package com.example.littleheightsacademy

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class ApplicationsViaGeoActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var studentTable: TableLayout
    private val nearbyAreas = listOf(
        "Strandfontein",
        "Pelican Heights",
        "Mitchells Plain",
        "Bayview",
        "Rocklands",
        "Westridge",
        "Portlands",
        "Lentegeur"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_applications_via_geo)

        studentTable = findViewById(R.id.studentTable)
        database = FirebaseDatabase.getInstance().getReference("students")

        fetchNearbyStudents()
    }

    private fun fetchNearbyStudents() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                studentTable.removeAllViews()

                // Add header row
                val header = TableRow(this@ApplicationsViaGeoActivity)
                arrayOf("Student", "Doc", "Status", "Date", "Action").forEach { h ->
                    val tv = TextView(this@ApplicationsViaGeoActivity)
                    tv.text = h
                    tv.setPadding(4,4,4,4)
                    tv.setBackgroundColor(0xFFECECEC.toInt())
                    tv.textSize = 12f
                    tv.gravity = Gravity.CENTER
                    header.addView(tv)
                }
                studentTable.addView(header)

                var hasAny = false

                for (child in snapshot.children) {
                    val student = child.getValue(Student::class.java)
                    student?.let {
                        // Safe assignment for id
                        if (it.id.isEmpty()) {
                            it.id = child.key ?: ""
                        }

                        // Check if address contains ANY nearby area and status is PENDING
                        val isNearby = nearbyAreas.any { area ->
                            it.address.contains(area, ignoreCase = true)
                        }

                        if (isNearby && it.status.equals("PENDING", ignoreCase = true)) {
                            addStudentRow(it)
                            hasAny = true
                        }
                    }
                }

                if (!hasAny) {
                    val emptyRow = TableRow(this@ApplicationsViaGeoActivity)
                    val tv = TextView(this@ApplicationsViaGeoActivity)
                    tv.text = "No pending applications in nearby areas."
                    tv.setPadding(8,8,8,8)
                    emptyRow.addView(tv)
                    studentTable.addView(emptyRow)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ApplicationsViaGeoActivity, "DB Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addStudentRow(student: Student) {
        val row = TableRow(this)
        row.setPadding(4,4,4,4)
        row.gravity = Gravity.CENTER_VERTICAL

        fun makeTextCell(text: String) = TextView(this).apply {
            this.text = text
            setPadding(4,4,4,4)
            textSize = 12f
        }

        // Student Name
        row.addView(makeTextCell("${student.firstName} ${student.lastName}"))
        // Doc button
        val viewBtn = Button(this).apply {
            text = "View"
            setOnClickListener { showStudentDetailsDialog(student) }
        }
        row.addView(viewBtn)
        // Status
        row.addView(makeTextCell(student.status))
        // Date (DOB)
        row.addView(makeTextCell(student.dob))
        // Action button
        val actionBtn = Button(this).apply {
            text = "Action"
            setOnClickListener { showStatusUpdateDialog(student) }
        }
        row.addView(actionBtn)

        studentTable.addView(row)
    }

    private fun showStudentDetailsDialog(student: Student) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("${student.firstName} ${student.lastName}")
        val message = """
            First Name: ${student.firstName}
            Last Name: ${student.lastName}
            Email: ${student.email}
            Address: ${student.address}
            ZIP: ${student.zip}
            DOB: ${student.dob}
            Status: ${student.status}
            Document: ${student.documentUrl ?: "Not uploaded"}
        """.trimIndent()
        builder.setMessage(message)
        builder.setPositiveButton("Open Document") { _, _ ->
            student.documentUrl?.let {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
                startActivity(intent)
            } ?: Toast.makeText(this, "No document uploaded", Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton("Close", null)
        builder.show()
    }

    private fun showStatusUpdateDialog(student: Student) {
        val options = arrayOf("APPROVED", "REJECTED")
        AlertDialog.Builder(this)
            .setTitle("Update Status for ${student.firstName}")
            .setItems(options) { _, which ->
                val newStatus = options[which]
                database.child(student.id).child("status").setValue(newStatus)
                    .addOnSuccessListener { Toast.makeText(this, "Status updated to $newStatus", Toast.LENGTH_SHORT).show() }
                    .addOnFailureListener { e -> Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_SHORT).show() }
            }.show()
    }
}

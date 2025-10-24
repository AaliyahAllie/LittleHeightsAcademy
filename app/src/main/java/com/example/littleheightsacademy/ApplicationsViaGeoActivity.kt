package com.example.littleheightsacademy

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil

class ApplicationsViaGeoActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var studentTable: TableLayout
    private var allStudents: MutableList<Student> = mutableListOf()

    // Reference location: Pelican Heights
    private val referenceLatLng = LatLng(-33.9337, 18.5570)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_applications_via_geo)

        database = FirebaseDatabase.getInstance().getReference("students")
        studentTable = findViewById(R.id.studentTable)

        fetchAllStudents()
    }

    private fun fetchAllStudents() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allStudents.clear()
                for (child in snapshot.children) {
                    val student = child.getValue(Student::class.java)
                    student?.let {
                        val studentLatLng = geocodeAddress(it.address, it.zip)
                        if (studentLatLng != null && distanceBetween(referenceLatLng, studentLatLng) <= 15_000.0) {
                            allStudents.add(it)
                        }
                    }
                }
                // Sort by closest distance
                allStudents.sortBy { geocodeAddress(it.address, it.zip)?.let { loc -> distanceBetween(referenceLatLng, loc) } }
                updateTable(allStudents)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ApplicationsViaGeoActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateTable(students: List<Student>) {
        studentTable.removeAllViews()
        // Header
        val header = TableRow(this)
        arrayOf("Student", "Doc", "Status", "Date", "Action").forEach { h ->
            val tv = TextView(this)
            tv.text = h
            tv.setPadding(4,4,4,4)
            tv.setBackgroundColor(0xFFECECEC.toInt())
            tv.textSize = 12f
            header.addView(tv)
        }
        studentTable.addView(header)

        students.forEach { addStudentRow(it) }
    }

    private fun addStudentRow(student: Student) {
        val row = TableRow(this)
        row.setPadding(4,4,4,4)
        row.gravity = Gravity.CENTER_VERTICAL

        val nameText = TextView(this)
        nameText.text = "${student.firstName} ${student.lastName}"
        row.addView(nameText)

        val viewBtn = Button(this)
        viewBtn.text = "View"
        viewBtn.setOnClickListener { showStudentDetailsDialog(student) }
        row.addView(viewBtn)

        val statusText = TextView(this)
        statusText.text = student.status ?: "PENDING"
        row.addView(statusText)

        val dobText = TextView(this)
        dobText.text = student.dob ?: "-"
        row.addView(dobText)

        val actionBtn = Button(this)
        actionBtn.text = "Action"
        actionBtn.setOnClickListener { showStatusUpdateDialog(student, statusText) }
        row.addView(actionBtn)

        studentTable.addView(row)
    }

    private fun showStudentDetailsDialog(student: Student) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("${student.firstName} ${student.lastName}")

        val message = StringBuilder()
        message.append("First Name: ${student.firstName}\n")
        message.append("Last Name: ${student.lastName}\n")
        message.append("Email: ${student.email}\n")
        message.append("Address: ${student.address}\n")
        message.append("ZIP: ${student.zip}\n")
        message.append("DOB: ${student.dob}\n")
        message.append("Activities: ${student.activities?.joinToString(", ") ?: "-"}\n")
        message.append("Status: ${student.status ?: "PENDING"}\n")
        message.append("Document: ${student.documentUrl ?: "Not uploaded"}")

        builder.setMessage(message.toString())
        builder.setPositiveButton("Open Document") { _, _ ->
            student.documentUrl?.let { url ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            } ?: Toast.makeText(this, "No document uploaded", Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton("Close", null)
        builder.show()
    }

    private fun showStatusUpdateDialog(student: Student, statusTextView: TextView) {
        val options = arrayOf("APPROVED", "REJECTED")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Update Status for ${student.firstName}")
        builder.setItems(options) { _, which ->
            val newStatus = options[which]
            database.child(student.id ?: return@setItems).child("status").setValue(newStatus)
                .addOnSuccessListener {
                    statusTextView.text = newStatus
                    Toast.makeText(this, "${student.firstName} status updated to $newStatus", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to update status: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
        builder.show()
    }

    // --- GEO UTILS ---
    private fun geocodeAddress(address: String, zip: String): LatLng? {
        // Only allow specific locations
        val locationMap = mapOf(
            "Strandfontein" to LatLng(-34.0500, 18.4500),
            "Muizenburg" to LatLng(-34.1000, 18.4500),
            "Pelican Heights" to LatLng(-33.9337, 18.5570),
            "Bayview" to LatLng(-33.9500, 18.5800),
            "Pelican Park" to LatLng(-33.9400, 18.5550)
        )

        // Match by address substring
        locationMap.forEach { (key, latLng) ->
            if (address.contains(key, ignoreCase = true)) return latLng
        }

        // Optional: match by ZIP if needed
        val zipMap = mapOf(
            "7941" to LatLng(-33.9337, 18.5570) // Pelican Heights
        )
        zipMap[zip]?.let { return it }

        return null // Not in allowed locations
    }

    private fun distanceBetween(start: LatLng, end: LatLng): Double {
        return SphericalUtil.computeDistanceBetween(start, end) // meters
    }
}

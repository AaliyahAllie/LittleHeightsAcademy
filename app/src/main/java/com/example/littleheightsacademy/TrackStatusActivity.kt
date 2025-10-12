package com.example.littleheightsacademy

import android.database.Cursor
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.littleheightsacademy.DatabaseHelper

class TrackStatusActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_track_status)

        dbHelper = DatabaseHelper(this)

        // 🔹 In a real app, you'd get this from the logged-in parent account
        val parentEmail = "parent@example.com"

        val studentContainer = findViewById<LinearLayout>(R.id.studentContainer)

        val cursor: Cursor = dbHelper.getStudentsByParentEmail(parentEmail)

        if (cursor.moveToFirst()) {
            do {
                val firstName = cursor.getString(cursor.getColumnIndexOrThrow("firstName"))
                val lastName = cursor.getString(cursor.getColumnIndexOrThrow("lastName"))
                val dob = cursor.getString(cursor.getColumnIndexOrThrow("dob"))
                val activities = cursor.getString(cursor.getColumnIndexOrThrow("activities"))
                val status = cursor.getString(cursor.getColumnIndexOrThrow("status"))

                // 🔹 Inflate the student card layout
                val studentView = layoutInflater.inflate(R.layout.item_student_status, studentContainer, false)

                // 🔹 Access views INSIDE that layout
                val nameView = studentView.findViewById<TextView>(R.id.tvStudentName)
                val dobView = studentView.findViewById<TextView>(R.id.tvDob)
                val actView = studentView.findViewById<TextView>(R.id.tvActivities)
                val statusView = studentView.findViewById<TextView>(R.id.tvStatus)

                // 🔹 Populate data
                nameView.text = "$firstName $lastName"
                dobView.text = "DOB: $dob"
                actView.text = "Activities: $activities"
                statusView.text = "Status: $status"

                // 🔹 Add this student card to the container
                studentContainer.addView(studentView)

            } while (cursor.moveToNext())
        } else {
            val noData = TextView(this)
            noData.text = "No students registered yet."
            noData.textSize = 16f
            studentContainer.addView(noData)
        }

        cursor.close()
    }
}

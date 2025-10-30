package com.example.littleheightsacademy

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.*

class AdminUpdateSeatsActivity : AppCompatActivity() {

    private lateinit var btnUpdate: Button
    private lateinit var txtSeatsLeft: TextView
    private lateinit var edtCapacityA: EditText
    private lateinit var edtAvailableA: EditText
    private lateinit var edtCapacityB: EditText
    private lateinit var edtAvailableB: EditText
    private lateinit var edtCapacityR: EditText
    private lateinit var edtAvailableR: EditText

    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_update_seats)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        database = FirebaseDatabase.getInstance().reference.child("seating")

        // Bind views
        btnUpdate = findViewById(R.id.btnUpdate)
        txtSeatsLeft = findViewById(R.id.txtSeatsLeft)
        edtCapacityA = findViewById(R.id.edtCapacityA)
        edtAvailableA = findViewById(R.id.edtAvailableA)
        edtCapacityB = findViewById(R.id.edtCapacityB)
        edtAvailableB = findViewById(R.id.edtAvailableB)
        edtCapacityR = findViewById(R.id.edtCapacityR)
        edtAvailableR = findViewById(R.id.edtAvailableR)

        loadSeatingData()

        btnUpdate.setOnClickListener {
            updateSeatingData()
        }

        //Bottom Navigation
        setupBottomNavigation()
    }

    private fun loadSeatingData() {
        database.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                edtCapacityA.setText(snapshot.child("A/capacity").getValue(Int::class.java)?.toString() ?: "0")
                edtAvailableA.setText(snapshot.child("A/available").getValue(Int::class.java)?.toString() ?: "0")
                edtCapacityB.setText(snapshot.child("B/capacity").getValue(Int::class.java)?.toString() ?: "0")
                edtAvailableB.setText(snapshot.child("B/available").getValue(Int::class.java)?.toString() ?: "0")
                edtCapacityR.setText(snapshot.child("R/capacity").getValue(Int::class.java)?.toString() ?: "0")
                edtAvailableR.setText(snapshot.child("R/available").getValue(Int::class.java)?.toString() ?: "0")

                val totalSeats = (edtCapacityA.text.toString().toIntOrNull() ?: 0) +
                        (edtCapacityB.text.toString().toIntOrNull() ?: 0) +
                        (edtCapacityR.text.toString().toIntOrNull() ?: 0)

                val totalAvailable = (edtAvailableA.text.toString().toIntOrNull() ?: 0) +
                        (edtAvailableB.text.toString().toIntOrNull() ?: 0) +
                        (edtAvailableR.text.toString().toIntOrNull() ?: 0)

                txtSeatsLeft.text = "Seats left: ${totalSeats - totalAvailable}"
            }
        }
    }

    private fun updateSeatingData() {
        val data = mapOf(
            "A" to mapOf(
                "capacity" to (edtCapacityA.text.toString().toIntOrNull() ?: 0),
                "available" to (edtAvailableA.text.toString().toIntOrNull() ?: 0)
            ),
            "B" to mapOf(
                "capacity" to (edtCapacityB.text.toString().toIntOrNull() ?: 0),
                "available" to (edtAvailableB.text.toString().toIntOrNull() ?: 0)
            ),
            "R" to mapOf(
                "capacity" to (edtCapacityR.text.toString().toIntOrNull() ?: 0),
                "available" to (edtAvailableR.text.toString().toIntOrNull() ?: 0)
            )
        )

        database.setValue(data).addOnSuccessListener {
            loadSeatingData()
        }
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

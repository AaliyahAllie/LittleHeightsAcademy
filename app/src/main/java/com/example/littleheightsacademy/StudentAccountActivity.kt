package com.example.littleheightsacademy

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class StudentAccountActivity : AppCompatActivity() {

    private lateinit var ivBack: ImageView
    private lateinit var ivProfile: ImageView
    private lateinit var tvName: TextView
    private lateinit var tvAddress: TextView
    private lateinit var tvGender: TextView
    private lateinit var tvAge: TextView
    private lateinit var etAllergy: EditText
    private lateinit var btnUpdate: Button
    private lateinit var btnUploadDocs: Button
    private lateinit var bottomNavigation: BottomNavigationView

    private lateinit var databaseRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var studentId: String? = null  // Will hold the student's database key

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_account)

        // Initialize views
        ivBack = findViewById(R.id.ivBack)
        ivProfile = findViewById(R.id.ivProfile)
        tvName = findViewById(R.id.tvName)
        tvAddress = findViewById(R.id.tvAddress)
        tvGender = findViewById(R.id.tvGender)
        tvAge = findViewById(R.id.tvAge)
        etAllergy = findViewById(R.id.etAllergy)
        btnUpdate = findViewById(R.id.btnUpdate)
        btnUploadDocs = findViewById(R.id.btnUploadDocs)
        bottomNavigation = findViewById(R.id.bottomNavigation)

        // Firebase setup
        auth = FirebaseAuth.getInstance()
        databaseRef = FirebaseDatabase.getInstance().getReference("students")

        val parentEmail = auth.currentUser?.email
        if (parentEmail.isNullOrEmpty()) {
            Toast.makeText(this, "Parent not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Back button logic
        ivBack.setOnClickListener { finish() }

        // Load student info linked to parent email
        loadStudentInfo(parentEmail)

        // Update button logic (update allergy info)
        btnUpdate.setOnClickListener {
            updateAllergyInfo()
        }

        // Upload documents button (logic can be added to open file picker)
        btnUploadDocs.setOnClickListener {
            Toast.makeText(this, "Upload documents clicked", Toast.LENGTH_SHORT).show()
        }

        // Bottom navigation logic (example)
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navHome -> { Toast.makeText(this, "Home clicked", Toast.LENGTH_SHORT).show() }
                R.id.navMenu -> { Toast.makeText(this, "Invoices clicked", Toast.LENGTH_SHORT).show() }
                R.id.nav_profile -> { Toast.makeText(this, "Profile clicked", Toast.LENGTH_SHORT).show() }
            }
            true
        }
    }

    private fun loadStudentInfo(parentEmail: String) {
        // Query the database for the child linked to the parent email
        val query = databaseRef.orderByChild("email").equalTo(parentEmail)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (child in snapshot.children) {
                        studentId = child.key
                        val data = child.value as Map<*, *>

                        val firstName = data["firstName"] as? String ?: ""
                        val lastName = data["lastName"] as? String ?: ""
                        val address = data["address"] as? String ?: ""
                        val dob = data["dob"] as? String ?: ""
                        val gender = data["gender"] as? String ?: "N/A"
                        val allergy = data["allergy"] as? String ?: ""

                        tvName.text = "$firstName $lastName"
                        tvAddress.text = "Address: $address"
                        tvGender.text = gender
                        tvAge.text = "DOB: $dob"
                        etAllergy.setText(allergy)
                    }
                } else {
                    Toast.makeText(this@StudentAccountActivity, "No student info found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@StudentAccountActivity, "Failed to load data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateAllergyInfo() {
        val allergyText = etAllergy.text.toString().trim()
        if (studentId.isNullOrEmpty()) {
            Toast.makeText(this, "Student ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        databaseRef.child(studentId!!).child("allergy").setValue(allergyText)
            .addOnSuccessListener {
                Toast.makeText(this, "Allergy info updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

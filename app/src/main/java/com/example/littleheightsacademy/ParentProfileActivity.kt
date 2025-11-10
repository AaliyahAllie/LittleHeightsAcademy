package com.example.littleheightsacademy

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ParentProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var ivBack: ImageView
    private lateinit var ivEditChildren: ImageView
    private lateinit var btnViewPayments: Button
    private lateinit var btnViewReports: Button
    private lateinit var btnRegisterStudent: Button
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var llChildrenContainer: LinearLayout
    private lateinit var tvParentName: TextView
    private lateinit var tvGender: TextView

    private lateinit var database: DatabaseReference

    private val parentEmail: String
        get() = auth.currentUser?.email ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parent_profile)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Initialize views
        ivBack = findViewById(R.id.ivBack)
        ivEditChildren = findViewById(R.id.ivEditChildren)
        btnViewPayments = findViewById(R.id.btnViewPayments)
        btnViewReports = findViewById(R.id.btnViewReports)
        btnRegisterStudent = findViewById(R.id.btnRegisterStudent)
        bottomNav = findViewById(R.id.bottomNavigation)
        llChildrenContainer = findViewById(R.id.llChildrenContainer)
        tvParentName = findViewById(R.id.tvParentName)
        tvGender = findViewById(R.id.tvGender)

        // Back button
        ivBack.setOnClickListener { finish() }

        // View payments
        btnViewPayments.setOnClickListener {
            startActivity(Intent(this, PaymentHistoryActivity::class.java))
        }

        // View student reports
        btnViewReports.setOnClickListener {
            startActivity(Intent(this, ParentViewReportsActivity::class.java))
        }

        // Register new student
        btnRegisterStudent.setOnClickListener {
            startActivity(Intent(this, RegisterStudentActivity::class.java))
        }

        // Edit children placeholder
        ivEditChildren.setOnClickListener {
            // TODO: Open EditChildrenActivity if implemented
        }

        // Bottom navigation
        bottomNav.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, ParentDashboardActivity::class.java))
                    true
                }
                R.id.nav_profile -> true
                R.id.nav_menu -> {
                    startActivity(Intent(this, NavigationActivity::class.java))
                    true
                }
                else -> false
            }
        }

        loadParentDetails()
        loadChildren()
    }

    private fun loadParentDetails() {
        val parentId = auth.currentUser?.uid ?: return

        database.child("parents").child(parentId).get()
            .addOnSuccessListener { snapshot ->
                val parentName = snapshot.child("firstName").getValue(String::class.java) ?: "Parent Name"
                val gender = snapshot.child("gender").getValue(String::class.java) ?: "Gender"

                tvParentName.text = parentName
                tvGender.text = gender
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load parent info", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadChildren() {
        database.child("students").get()
            .addOnSuccessListener { snapshot ->
                llChildrenContainer.removeAllViews()
                val children = snapshot.children.mapNotNull { snap ->
                    snap.getValue(Student::class.java)?.takeIf { it.email == parentEmail }
                }

                if (children.isEmpty()) {
                    val noChildren = TextView(this)
                    noChildren.text = "No children registered."
                    llChildrenContainer.addView(noChildren)
                    return@addOnSuccessListener
                }

                children.forEachIndexed { index, child ->
                    val tvChild = TextView(this)
                    tvChild.text = "${index + 1}. ${child.firstName} ${child.lastName}"
                    tvChild.textSize = 16f
                    tvChild.setPadding(0, 8, 0, 4)
                    llChildrenContainer.addView(tvChild)

                    val btnViewAccount = Button(this)
                    btnViewAccount.text = "View Student Account"
                    btnViewAccount.setOnClickListener {
                        val intent = Intent(this, StudentAccountActivity::class.java)
                        intent.putExtra("studentId", child.id)
                        startActivity(intent)
                    }
                    llChildrenContainer.addView(btnViewAccount)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load children", Toast.LENGTH_SHORT).show()
            }
    }
}

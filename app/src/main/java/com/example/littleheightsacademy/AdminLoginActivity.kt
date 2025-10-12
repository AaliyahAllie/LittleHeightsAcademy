package com.example.littleheightsacademy

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class AdminLoginActivity : AppCompatActivity() {

    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvRegisterLink: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        email = findViewById(R.id.etEmail)
        password = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvRegisterLink = findViewById(R.id.tvRegisterLink)

        btnLogin.setOnClickListener {
            loginAdmin()
        }

        tvRegisterLink.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loginAdmin() {
        val mail = email.text.toString().trim()
        val pass = password.text.toString().trim()

        if (mail.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(mail, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener

                    // Check if user role is Admin in Firebase
                    val adminRef = database.child("admins").child(userId).child("role")

                    adminRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val role = snapshot.getValue(String::class.java)
                            if (role == "Admin") {
                                Toast.makeText(
                                    this@AdminLoginActivity,
                                    "Welcome, Admin!",
                                    Toast.LENGTH_SHORT
                                ).show()


                                val intent = Intent(
                                    this@AdminLoginActivity,
                                    AdminDashboardActivity::class.java
                                )
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(
                                    this@AdminLoginActivity,
                                    "Access denied. Admins only.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                auth.signOut()
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(
                                this@AdminLoginActivity,
                                "Database error: ${error.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
                } else {
                    val errorMessage =
                        task.exception?.localizedMessage ?: "Login failed. Please try again."
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
    }
}

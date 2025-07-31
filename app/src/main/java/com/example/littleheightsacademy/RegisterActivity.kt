package com.example.littleheightsacademy

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var firstName: EditText
    private lateinit var lastName: EditText
    private lateinit var email: EditText
    private lateinit var address: EditText
    private lateinit var phone: EditText
    private lateinit var username: EditText
    private lateinit var password: EditText
    private lateinit var radioGroup: RadioGroup
    private lateinit var btnCreateAccount: Button

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        firstName = findViewById(R.id.etFirstName)
        lastName = findViewById(R.id.etLastName)
        email = findViewById(R.id.etEmail)
        address = findViewById(R.id.etAddress)
        phone = findViewById(R.id.etPhone)
        username = findViewById(R.id.etUsername)
        password = findViewById(R.id.etPassword)
        radioGroup = findViewById(R.id.radioGroupRole)
        btnCreateAccount = findViewById(R.id.btnCreateAccount)

        btnCreateAccount.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        val role = when (radioGroup.checkedRadioButtonId) {
            R.id.radioParent -> "Parent"
            R.id.radioAdmin -> "Admin"
            else -> null
        }

        val fName = firstName.text.toString().trim()
        val lName = lastName.text.toString().trim()
        val mail = email.text.toString().trim()
        val addr = address.text.toString().trim()
        val ph = phone.text.toString().trim()
        val uname = username.text.toString().trim()
        val pass = password.text.toString().trim()

        if (role == null || fName.isEmpty() || lName.isEmpty() || mail.isEmpty()
            || addr.isEmpty() || ph.isEmpty() || uname.isEmpty() || pass.isEmpty()
        ) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (pass.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(mail, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: uname

                    val userMap = mapOf(
                        "firstName" to fName,
                        "lastName" to lName,
                        "email" to mail,
                        "address" to addr,
                        "phone" to ph,
                        "username" to uname,
                        "role" to role
                    )

                    FirebaseDatabase.getInstance().reference
                        .child("users")
                        .child(userId)
                        .setValue(userMap)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()

                            val intent = if (role == "Parent") {
                                Intent(this, ParentLoginActivity::class.java)
                            } else {
                                Intent(this, AdminLoginActivity::class.java)
                            }
                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    val errorMessage = task.exception?.localizedMessage ?: "Registration failed"
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
    }
}

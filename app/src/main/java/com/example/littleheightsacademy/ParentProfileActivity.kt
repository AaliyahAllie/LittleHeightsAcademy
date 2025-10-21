package com.example.littleheightsacademy

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class ParentProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parent_profile)

        val ivBack: ImageView = findViewById(R.id.ivBack)
        val btnViewPayments: Button = findViewById(R.id.btnViewPayments)
        val btnMakePayment: Button = findViewById(R.id.btnMakePayment)
        val btnRegisterStudent: Button = findViewById(R.id.btnRegisterStudent)
        val ivEditChildren: ImageView = findViewById(R.id.ivEditChildren)

        ivBack.setOnClickListener {
            finish()
        }

        btnViewPayments.setOnClickListener {
            startActivity(Intent(this, PaymentHistoryActivity::class.java))
        }

        btnMakePayment.setOnClickListener {
            startActivity(Intent(this, PaymentHistoryActivity::class.java))
        }

        btnRegisterStudent.setOnClickListener {
            startActivity(Intent(this, RegisterStudentActivity::class.java))
        }

        ivEditChildren.setOnClickListener {
            // open edit children activity or dialog
            //startActivity(Intent(this, EditChildrenActivity::class.java))
        }
    }
}

package com.example.littleheightsacademy

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class AdminInvoiceActivity : AppCompatActivity() {

    private lateinit var edtInvoiceId: EditText
    private lateinit var edtParentEmail: EditText
    private lateinit var edtDate: EditText
    private lateinit var edtAmount: EditText
    private lateinit var edtStatus: EditText
    private lateinit var edtDescription: EditText
    private lateinit var btnUploadInvoice: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_invoice)

        edtInvoiceId = findViewById(R.id.edtInvoiceId)
        edtParentEmail = findViewById(R.id.edtParentEmail)
        edtDate = findViewById(R.id.edtDate)
        edtAmount = findViewById(R.id.edtAmount)
        edtStatus = findViewById(R.id.edtStatus)
        edtDescription = findViewById(R.id.edtDescription)
        btnUploadInvoice = findViewById(R.id.btnUploadInvoice)

        btnUploadInvoice.setOnClickListener {
            uploadInvoice()
        }
    }

    private fun uploadInvoice() {
        val invoiceId = edtInvoiceId.text.toString().trim()
        val parentEmail = edtParentEmail.text.toString().trim()
        val date = edtDate.text.toString().trim()
        val amount = edtAmount.text.toString().trim()
        val status = edtStatus.text.toString().trim()
        val description = edtDescription.text.toString().trim()

        if (invoiceId.isEmpty() || parentEmail.isEmpty() || date.isEmpty() || amount.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val databaseRef = FirebaseDatabase.getInstance().getReference("invoices")
        val invoice = Invoice(invoiceId, parentEmail, date, amount, status, description)

        databaseRef.child(invoiceId).setValue(invoice)
            .addOnSuccessListener {
                Toast.makeText(this, "Invoice uploaded successfully", Toast.LENGTH_SHORT).show()
                clearFields()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to upload: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearFields() {
        edtInvoiceId.text.clear()
        edtParentEmail.text.clear()
        edtDate.text.clear()
        edtAmount.text.clear()
        edtStatus.text.clear()
        edtDescription.text.clear()
    }
}

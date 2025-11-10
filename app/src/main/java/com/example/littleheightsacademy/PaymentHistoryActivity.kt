package com.example.littleheightsacademy

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.io.File
import java.io.FileOutputStream

class PaymentHistoryActivity : AppCompatActivity() {

    private lateinit var recyclerInvoices: RecyclerView
    private lateinit var databaseRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private val invoiceList = mutableListOf<Invoice>()
    private lateinit var adapter: InvoiceAdapter

    companion object {
        const val STORAGE_PERMISSION_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_history)

        recyclerInvoices = findViewById(R.id.recyclerInvoices)
        recyclerInvoices.layoutManager = LinearLayoutManager(this)
        adapter = InvoiceAdapter(invoiceList)
        recyclerInvoices.adapter = adapter

        auth = FirebaseAuth.getInstance()
        val parentEmail = auth.currentUser?.email

        if (parentEmail.isNullOrEmpty()) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        databaseRef = FirebaseDatabase.getInstance().getReference("invoices")

        // Load invoices
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                invoiceList.clear()
                for (child in snapshot.children) {
                    val invoice = child.getValue(Invoice::class.java)
                    if (invoice != null && invoice.parentEmail.equals(parentEmail, ignoreCase = true)) {
                        invoiceList.add(invoice)
                    }
                }
                if (invoiceList.isEmpty()) {
                    Toast.makeText(this@PaymentHistoryActivity, "No invoices found.", Toast.LENGTH_SHORT).show()
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@PaymentHistoryActivity,
                    "Failed to load invoices: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        // Request storage permission for downloads
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    STORAGE_PERMISSION_CODE
                )
            }
        }

        // Handle invoice clicks
        adapter.onInvoiceClick = { invoice ->
            showInvoiceDialog(invoice)
        }
    }

    private fun showInvoiceDialog(invoice: Invoice) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Invoice Details")

        val message = """
            Invoice ID: ${invoice.invoiceId}
            Parent Email: ${invoice.parentEmail}
            Amount: ${invoice.amount}
            Date: ${invoice.date}
        """.trimIndent()

        builder.setMessage(message)
        builder.setPositiveButton("Download") { _, _ ->
            saveInvoiceToDevice(invoice)
        }
        builder.setNegativeButton("Close", null)
        builder.show()
    }

    private fun saveInvoiceToDevice(invoice: Invoice) {
        try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, "Invoice_${invoice.invoiceId}.txt")
            val fos = FileOutputStream(file)
            val content = """
                Invoice ID: ${invoice.invoiceId}
                Parent Email: ${invoice.parentEmail}
                Amount: ${invoice.amount}
                Date: ${invoice.date}
            """.trimIndent()
            fos.write(content.toByteArray())
            fos.close()
            Toast.makeText(this, "Invoice saved: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to save invoice: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}

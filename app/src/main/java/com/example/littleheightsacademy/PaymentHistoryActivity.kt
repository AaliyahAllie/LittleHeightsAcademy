package com.example.littleheightsacademy

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class PaymentHistoryActivity : AppCompatActivity() {

    private lateinit var recyclerInvoices: RecyclerView
    private lateinit var invoiceList: MutableList<Invoice>
    private lateinit var adapter: InvoiceAdapter
    private lateinit var databaseRef: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_history)

        // Initialize views
        recyclerInvoices = findViewById(R.id.recyclerInvoices)
        recyclerInvoices.layoutManager = LinearLayoutManager(this)
        invoiceList = mutableListOf()
        adapter = InvoiceAdapter(invoiceList)
        recyclerInvoices.adapter = adapter

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        val parentEmail = auth.currentUser?.email

        if (parentEmail.isNullOrEmpty()) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Reference invoices in Realtime Database
        databaseRef = FirebaseDatabase.getInstance().getReference("invoices")

        // Load invoices filtered by the logged-in user's email
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
    }
}

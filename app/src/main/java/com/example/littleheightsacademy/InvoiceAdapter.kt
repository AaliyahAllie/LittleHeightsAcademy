package com.example.littleheightsacademy

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class InvoiceAdapter(private val invoices: List<Invoice>) : RecyclerView.Adapter<InvoiceAdapter.InvoiceViewHolder>() {

    var onInvoiceClick: ((Invoice) -> Unit)? = null

    inner class InvoiceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtInvoiceId: TextView = view.findViewById(R.id.txtInvoiceId)
        val txtAmount: TextView = view.findViewById(R.id.txtAmount)
        val btnView: Button = view.findViewById(R.id.btnView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvoiceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_invoice, parent, false)
        return InvoiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: InvoiceViewHolder, position: Int) {
        val invoice = invoices[position]
        holder.txtInvoiceId.text = "Invoice: ${invoice.invoiceId}"
        holder.txtAmount.text = "Amount: ${invoice.amount}"
        holder.btnView.setOnClickListener {
            onInvoiceClick?.invoke(invoice)
        }
    }

    override fun getItemCount(): Int = invoices.size
}

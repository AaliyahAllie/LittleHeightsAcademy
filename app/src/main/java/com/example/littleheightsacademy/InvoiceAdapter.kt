package com.example.littleheightsacademy

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class InvoiceAdapter(private val invoices: List<Invoice>) :
    RecyclerView.Adapter<InvoiceAdapter.InvoiceViewHolder>() {

    class InvoiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtInvoiceId: TextView = itemView.findViewById(R.id.txtInvoiceId)
        val txtDate: TextView = itemView.findViewById(R.id.txtDate)
        val txtAmount: TextView = itemView.findViewById(R.id.txtAmount)
        val txtStatus: TextView = itemView.findViewById(R.id.txtStatus)
        val txtDescription: TextView = itemView.findViewById(R.id.txtDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvoiceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_invoice, parent, false)
        return InvoiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: InvoiceViewHolder, position: Int) {
        val invoice = invoices[position]
        holder.txtInvoiceId.text = "Invoice: ${invoice.invoiceId}"
        holder.txtDate.text = "Date: ${invoice.date}"
        holder.txtAmount.text = "Amount: R${invoice.amount}"
        holder.txtStatus.text = "Status: ${invoice.status}"
        holder.txtDescription.text = "Description: ${invoice.description}"
    }

    override fun getItemCount(): Int = invoices.size
}

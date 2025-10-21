package com.example.littleheightsacademy

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PaymentAdapter(
    private val payments: List<Payment>,
    private val onDownloadClick: (Payment) -> Unit
) : RecyclerView.Adapter<PaymentAdapter.PaymentViewHolder>() {

    inner class PaymentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtStudentName: TextView = itemView.findViewById(R.id.txtStudentName)
        val txtDate: TextView = itemView.findViewById(R.id.txtDate)
        val txtDownload: TextView = itemView.findViewById(R.id.txtDownload)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_payment, parent, false)
        return PaymentViewHolder(view)
    }

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        val payment = payments[position]
        holder.txtStudentName.text = payment.studentName
        holder.txtDate.text = payment.date
        holder.txtDownload.setOnClickListener {
            onDownloadClick(payment)
        }
    }

    override fun getItemCount() = payments.size
}

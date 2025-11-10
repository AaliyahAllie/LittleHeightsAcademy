package com.example.littleheightsacademy

data class Invoice(
    val invoiceId: String = "",
    val parentEmail: String = "",
    val date: String = "",
    val amount: String = "",
    val status: String = "",
    val description: String = ""
)

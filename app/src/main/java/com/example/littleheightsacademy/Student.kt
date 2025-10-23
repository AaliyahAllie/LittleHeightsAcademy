package com.example.littleheightsacademy

data class Student(
    var id: String = "",
    var firstName: String = "",
    var lastName: String = "",
    var email: String = "",
    var address: String = "",
    var zip: String = "",
    var dob: String = "",
    var activities: List<String> = listOf(),
    var status: String = "PENDING",
    var documentUrl: String? = null // optional
)

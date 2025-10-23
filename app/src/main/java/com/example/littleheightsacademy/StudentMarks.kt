package com.example.littleheightsacademy

// Extended model for marks
data class StudentMarks(
    val student: Student,
    var classMark: Int = 0,
    var islamicMark: Int = 0
) {
    val name: String
        get() = "${student.firstName} ${student.lastName}"
}

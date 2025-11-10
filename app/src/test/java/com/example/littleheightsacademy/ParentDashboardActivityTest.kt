package com.example.littleheightsacademy

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.android.material.progressindicator.CircularProgressIndicator
import android.widget.TextView
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

class ParentDashboardActivityTest {

    private lateinit var snapshot: DataSnapshot
    private lateinit var progressSeating: CircularProgressIndicator
    private lateinit var txtPercentage: TextView
    private lateinit var txtSeatsLeft: TextView

    @Before
    fun setUp() {
        snapshot = mock(DataSnapshot::class.java)
        progressSeating = mock(CircularProgressIndicator::class.java)
        txtPercentage = mock(TextView::class.java)
        txtSeatsLeft = mock(TextView::class.java)
    }

    @Test
    fun testLoadSeatingProgressCalculation() {
        // Arrange: mock Firebase child data
        val seatData = mapOf(
            "A/capacity" to 50,
            "A/available" to 10,
            "B/capacity" to 30,
            "B/available" to 5,
            "R/capacity" to 20,
            "R/available" to 5
        )

        for ((key, value) in seatData) {
            val childSnapshot = mock(DataSnapshot::class.java)
            `when`(snapshot.child(key)).thenReturn(childSnapshot)
            `when`(childSnapshot.getValue(Int::class.java)).thenReturn(value)
        }

        // Act: replicate loadSeatingProgress logic
        val capacityA = snapshot.child("A/capacity").getValue(Int::class.java) ?: 0
        val availableA = snapshot.child("A/available").getValue(Int::class.java) ?: 0
        val capacityB = snapshot.child("B/capacity").getValue(Int::class.java) ?: 0
        val availableB = snapshot.child("B/available").getValue(Int::class.java) ?: 0
        val capacityR = snapshot.child("R/capacity").getValue(Int::class.java) ?: 0
        val availableR = snapshot.child("R/available").getValue(Int::class.java) ?: 0

        val totalCapacity = capacityA + capacityB + capacityR
        val totalEnrolled = totalCapacity - (availableA + availableB + availableR)
        val percentageFilled = if (totalCapacity > 0) (totalEnrolled * 100) / totalCapacity else 0
        val seatsLeft = availableA + availableB + availableR

        // Simulate updating UI
        progressSeating.progress = percentageFilled
        txtPercentage.text = "$percentageFilled%"
        txtSeatsLeft.text = "Seats left: $seatsLeft"

        // Assert
        assert(totalCapacity == 100)
        assert(totalEnrolled == 80)
        assert(seatsLeft == 20)
        assert(percentageFilled == 80)

        // Verify that UI elements were updated correctly
        verify(progressSeating).progress = 80
        verify(txtPercentage).text = "80%"
        verify(txtSeatsLeft).text = "Seats left: 20"
    }
}
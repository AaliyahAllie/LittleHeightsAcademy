package com.example.littleheightsacademy

import com.google.firebase.database.DataSnapshot
import org.junit.Test
import org.mockito.Mockito.*

class AdminDashboardActivityTest {

    @Test
    fun testSeatingDataCalculationLogic() {
        // Arrange
        val snapshot = mock(DataSnapshot::class.java)

        // Mock Firebase children values
        val seatData = mapOf(
            "A/capacity" to 50,
            "A/available" to 10,
            "B/capacity" to 30,
            "B/available" to 5,
            "R/capacity" to 20,
            "R/available" to 5
        )

        // Mock snapshot.child("path").getValue(Int::class.java) for each key
        for ((key, value) in seatData) {
            val childSnapshot = mock(DataSnapshot::class.java)
            `when`(snapshot.child(key)).thenReturn(childSnapshot)
            `when`(childSnapshot.getValue(Int::class.java)).thenReturn(value)
        }

        // Act: replicate the same logic from loadSeatingData()
        val capacityA = snapshot.child("A/capacity").getValue(Int::class.java) ?: 0
        val availableA = snapshot.child("A/available").getValue(Int::class.java) ?: 0
        val capacityB = snapshot.child("B/capacity").getValue(Int::class.java) ?: 0
        val availableB = snapshot.child("B/available").getValue(Int::class.java) ?: 0
        val capacityR = snapshot.child("R/capacity").getValue(Int::class.java) ?: 0
        val availableR = snapshot.child("R/available").getValue(Int::class.java) ?: 0

        val totalCapacity = capacityA + capacityB + capacityR
        val totalEnrolled = totalCapacity - (availableA + availableB + availableR)
        val seatsLeft = totalCapacity - totalEnrolled
        val percentageFilled = if (totalCapacity > 0) (totalEnrolled * 100) / totalCapacity else 0

        // Assert
        assert(totalCapacity == 100)
        assert(totalEnrolled == 80)
        assert(seatsLeft == 20)
        assert(percentageFilled == 80)
    }
}
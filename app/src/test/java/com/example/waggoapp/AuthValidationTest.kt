package com.example.waggoapp

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthValidationTest {

    // Backend regex from RegisterRequest.java:
    // ^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z\d]).{12,72}$
    private val passwordRegex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{12,72}$")

    @Test
    fun testValidPasswords() {
        assertTrue(passwordRegex.matches("WashGo#2026!Secured"))
        assertTrue(passwordRegex.matches("P@ssw0rdP@ssw0rd"))
        assertTrue(passwordRegex.matches("SuperSecret!1234"))
    }

    @Test
    fun testTooShortPassword() {
        // Less than 12 characters
        assertFalse(passwordRegex.matches("Short!1a"))
        assertFalse(passwordRegex.matches("Abc!1234567"))
    }

    @Test
    fun testMissingCharacterClasses() {
        // Missing uppercase
        assertFalse(passwordRegex.matches("washgo#2026!secured"))
        // Missing lowercase
        assertFalse(passwordRegex.matches("WASHGO#2026!SECURED"))
        // Missing digit
        assertFalse(passwordRegex.matches("WashGo#Secured!Here"))
        // Missing special character
        assertFalse(passwordRegex.matches("WashGo2026Secured"))
    }

    @Test
    fun testNameSplittingLogic() {
        val fullName = "John Michael Doe"
        val parts = fullName.trim().split("\\s+".toRegex())
        val firstName = parts.firstOrNull().orEmpty()
        val lastName = if (parts.size > 1) parts.drop(1).joinToString(" ") else "Customer"

        org.junit.Assert.assertEquals("John", firstName)
        org.junit.Assert.assertEquals("Michael Doe", lastName)

        val singleName = "Cher"
        val singleParts = singleName.trim().split("\\s+".toRegex())
        val singleFirst = singleParts.firstOrNull().orEmpty()
        val singleLast = if (singleParts.size > 1) singleParts.drop(1).joinToString(" ") else "Customer"

        org.junit.Assert.assertEquals("Cher", singleFirst)
        org.junit.Assert.assertEquals("Customer", singleLast)
    }
}

package com.example.skalelit

import org.junit.Test

import org.junit.Assert.*

import org.junit.Assert.assertEquals // Needed for checking values
import org.junit.Assert.assertNotEquals

class SecurityTest {
    @Test
    fun password_hashing_is_consistent() {
        val password = "mySecretPassword123"

        // Hash it once
        val hash1 = SecurityUtils.hashPassword(password)
        // Hash it again
        val hash2 = SecurityUtils.hashPassword(password)

        // 1. Check consistency: Hashing the same password twice must give the SAME result
        assertEquals(hash1, hash2)

        // 2. Check security: The hash must NOT look like the original password
        assertNotEquals(password, hash1)

        // Print results to the console below
        println("Original: $password")
        println("Hashed:   $hash1")
    }
}
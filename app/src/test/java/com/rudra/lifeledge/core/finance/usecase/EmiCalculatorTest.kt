package com.rudra.lifeledge.core.finance.usecase

import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs

/**
 * Unit tests for EmiCalculator
 */
class EmiCalculatorTest {
    
    @Test
    fun `calculateEmi should return correct EMI for standard loan`() {
        // Test case: 100,000 at 10% for 12 months
        // EMI = 100000 * 0.008333 * (1.008333)^12 / ((1.008333)^12 - 1)
        // Expected EMI ≈ 8,791.59
        val emi = EmiCalculator.calculateEmi(
            principal = 100000.0,
            annualInterestRate = 10.0,
            tenureMonths = 12
        )
        
        assertEquals(8791.59, emi, 1.0) // Allow 1 taka tolerance
    }
    
    @Test
    fun `calculateEmi should return zero for zero principal`() {
        val emi = EmiCalculator.calculateEmi(
            principal = 0.0,
            annualInterestRate = 10.0,
            tenureMonths = 12
        )
        
        assertEquals(0.0, emi, 0.01)
    }
    
    @Test
    fun `calculateEmi should return zero for negative principal`() {
        val emi = EmiCalculator.calculateEmi(
            principal = -100000.0,
            annualInterestRate = 10.0,
            tenureMonths = 12
        )
        
        assertEquals(0.0, emi, 0.01)
    }
    
    @Test
    fun `calculateEmi should return zero for zero tenure`() {
        val emi = EmiCalculator.calculateEmi(
            principal = 100000.0,
            annualInterestRate = 10.0,
            tenureMonths = 0
        )
        
        assertEquals(0.0, emi, 0.01)
    }
    
    @Test
    fun `calculateEmi should return simple division for zero interest rate`() {
        val emi = EmiCalculator.calculateEmi(
            principal = 120000.0,
            annualInterestRate = 0.0,
            tenureMonths = 12
        )
        
        assertEquals(10000.0, emi, 0.01) // 120000 / 12
    }
    
    @Test
    fun `calculateEmi should handle high interest rates`() {
        val emi = EmiCalculator.calculateEmi(
            principal = 50000.0,
            annualInterestRate = 24.0, // 24% per year
            tenureMonths = 6
        )
        
        assertTrue(emi > 0)
        assertTrue(emi > 50000.0 / 6) // Should be more than simple division due to interest
    }
    
    @Test
    fun `calculateEmiBreakdown should calculate correct interest`() {
        // For 100,000 at 10% annual rate
        // Monthly interest = 100000 * 10/100/12 = 833.33
        val breakdown = EmiCalculator.calculateEmiBreakdown(
            principal = 100000.0,
            annualInterestRate = 10.0
        )
        
        assertEquals(833.33, breakdown.interest, 1.0)
    }
    
    @Test
    fun `calculatePaymentBreakdown should split EMI correctly`() {
        // Test with a known EMI
        val emi = 8791.59
        val principal = 100000.0
        val rate = 10.0
        
        val breakdown = EmiCalculator.calculatePaymentBreakdown(
            principal = principal,
            annualInterestRate = rate,
            emi = emi
        )
        
        // Interest for first month = 100000 * 10/100/12 = 833.33
        val expectedInterest = 833.33
        val expectedPrincipal = emi - expectedInterest
        
        assertEquals(expectedInterest, breakdown.interest, 1.0)
        assertEquals(expectedPrincipal, breakdown.principal, 1.0)
        assertEquals(emi, breakdown.total, 0.1)
    }
    
    @Test
    fun `calculatePaymentBreakdown should handle final payment correctly`() {
        // When principal is less than EMI - interest
        val principal = 1000.0
        val rate = 10.0
        val emi = 10000.0 // Much higher than remaining balance
        
        val breakdown = EmiCalculator.calculatePaymentBreakdown(
            principal = principal,
            annualInterestRate = rate,
            emi = emi
        )
        
        // Should not pay more than remaining principal
        assertTrue(breakdown.principal <= principal)
    }
    
    @Test
    fun `calculateTotalInterest should calculate correct total interest`() {
        val emi = 8791.59
        val principal = 100000.0
        val tenure = 12
        
        val totalInterest = EmiCalculator.calculateTotalInterest(
            principal = principal,
            emi = emi,
            tenureMonths = tenure
        )
        
        // Total paid = 8791.59 * 12 = 105,499.08
        // Interest = 105,499.08 - 100,000 = 5,499.08
        val expectedTotalInterest = (emi * tenure) - principal
        
        assertEquals(expectedTotalInterest, totalInterest, 0.1)
    }
    
    @Test
    fun `calculateTotalPayment should calculate correct total payment`() {
        val emi = 8791.59
        val tenure = 12
        
        val totalPayment = EmiCalculator.calculateTotalPayment(
            emi = emi,
            tenureMonths = tenure
        )
        
        assertEquals(8791.59 * 12, totalPayment, 0.1)
    }
    
    @Test
    fun `calculateTenure should calculate correct tenure for zero interest`() {
        val tenure = EmiCalculator.calculateTenure(
            principal = 120000.0,
            annualInterestRate = 0.0,
            emi = 10000.0
        )
        
        assertEquals(12.0, tenure, 0.1)
    }
    
    @Test
    fun `calculateTenure should calculate correct tenure with interest`() {
        // For 100,000 at 10% with EMI of 8,791.59
        // Should be exactly 12 months
        val tenure = EmiCalculator.calculateTenure(
            principal = 100000.0,
            annualInterestRate = 10.0,
            emi = 8791.59
        )
        
        assertEquals(12.0, tenure, 0.5) // Allow small floating point error
    }
    
    @Test
    fun `EMI should increase with higher principal`() {
        val emi1 = EmiCalculator.calculateEmi(50000.0, 10.0, 12)
        val emi2 = EmiCalculator.calculateEmi(100000.0, 10.0, 12)
        
        assertTrue(emi2 > emi1)
    }
    
    @Test
    fun `EMI should increase with higher interest rate`() {
        val emi1 = EmiCalculator.calculateEmi(100000.0, 5.0, 12)
        val emi2 = EmiCalculator.calculateEmi(100000.0, 15.0, 12)
        
        assertTrue(emi2 > emi1)
    }
    
    @Test
    fun `EMI should decrease with longer tenure`() {
        val emi1 = EmiCalculator.calculateEmi(100000.0, 10.0, 6)
        val emi2 = EmiCalculator.calculateEmi(100000.0, 10.0, 24)
        
        assertTrue(emi1 > emi2)
    }
}

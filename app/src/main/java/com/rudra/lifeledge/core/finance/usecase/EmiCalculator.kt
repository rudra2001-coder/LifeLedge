package com.rudra.lifeledge.core.finance.usecase

import kotlin.math.pow

/**
 * EMI (Equated Monthly Installment) Calculator.
 * 
 * EMI Formula:
 * EMI = [P × r × (1 + r)^n] / [(1 + r)^n - 1]
 * 
 * Where:
 * - P = Principal loan amount
 * - r = Monthly interest rate (annual rate / 12 / 100)
 * - n = Number of monthly installments
 */
object EmiCalculator {
    
    /**
     * Calculates the monthly EMI amount.
     * 
     * @param principal The loan principal amount
     * @param annualInterestRate The annual interest rate (in percentage, e.g., 12.5 for 12.5%)
     * @param tenureMonths The loan tenure in months
     * @return The monthly EMI amount
     */
    fun calculateEmi(
        principal: Double,
        annualInterestRate: Double,
        tenureMonths: Int
    ): Double {
        if (principal <= 0 || tenureMonths <= 0) return 0.0
        
        // If no interest rate, simple division
        if (annualInterestRate <= 0) {
            return principal / tenureMonths
        }
        
        val monthlyRate = annualInterestRate / 12 / 100
        val onePlusR = 1 + monthlyRate
        
        val emi = principal * monthlyRate * onePlusR.pow(tenureMonths)
        val denominator = onePlusR.pow(tenureMonths) - 1
        
        return emi / denominator
    }
    
    /**
     * Calculates the breakdown of an EMI payment into principal and interest components.
     * 
     * @param principal The remaining principal balance
     * @param annualInterestRate The annual interest rate (in percentage)
     * @return EMIBreakdown containing principal and interest amounts
     */
    fun calculateEmiBreakdown(
        principal: Double,
        annualInterestRate: Double
    ): EMIBreakdown {
        if (principal <= 0) {
            return EMIBreakdown(principal = 0.0, interest = 0.0)
        }
        
        val monthlyRate = annualInterestRate / 12 / 100
        val interest = principal * monthlyRate
        
        return EMIBreakdown(
            principal = principal, // This will be overridden by actual EMI calculation
            interest = interest
        )
    }
    
    /**
     * Calculates the EMI breakdown for a specific payment number.
     * 
     * @param principal The remaining principal balance
     * @param annualInterestRate The annual interest rate (in percentage)
     * @param emi The monthly EMI amount
     * @return EMIBreakdown with principal and interest for this payment
     */
    fun calculatePaymentBreakdown(
        principal: Double,
        annualInterestRate: Double,
        emi: Double
    ): EMIBreakdown {
        if (principal <= 0 || emi <= 0) {
            return EMIBreakdown(principal = 0.0, interest = 0.0)
        }
        
        val monthlyRate = annualInterestRate / 12 / 100
        val interest = principal * monthlyRate
        
        // Principal portion is EMI minus interest
        // But we can't pay more than the remaining principal
        val principalPortion = minOf(emi - interest, principal)
        
        return EMIBreakdown(
            principal = principalPortion,
            interest = interest
        )
    }
    
    /**
     * Calculates the total interest paid over the loan tenure.
     * 
     * @param principal The loan principal amount
     * @param emi The monthly EMI amount
     * @param tenureMonths The loan tenure in months
     * @return Total interest paid
     */
    fun calculateTotalInterest(
        principal: Double,
        emi: Double,
        tenureMonths: Int
    ): Double {
        return (emi * tenureMonths) - principal
    }
    
    /**
     * Calculates the total amount paid (principal + interest) over the loan tenure.
     * 
     * @param emi The monthly EMI amount
     * @param tenureMonths The loan tenure in months
     * @return Total amount paid
     */
    fun calculateTotalPayment(
        emi: Double,
        tenureMonths: Int
    ): Double {
        return emi * tenureMonths
    }
    
    /**
     * Calculates how many months it will take to repay a loan with a given EMI.
     * 
     * @param principal The loan principal amount
     * @param annualInterestRate The annual interest rate (in percentage)
     * @param emi The monthly EMI amount
     * @return Number of months required (may be partial)
     */
    fun calculateTenure(
        principal: Double,
        annualInterestRate: Double,
        emi: Double
    ): Double {
        if (emi <= principal) {
            // Simple case: EMI covers at least the principal
            return if (annualInterestRate <= 0) {
                principal / emi
            } else {
                // Need to use logarithmic formula
                val monthlyRate = annualInterestRate / 12 / 100
                val n = -Math.log(1 - (principal * monthlyRate / emi)) / Math.log(1 + monthlyRate)
                n
            }
        }
        return 0.0 // EMI is less than interest - loan never gets paid
    }
    
    /**
     * Data class representing the breakdown of an EMI payment.
     */
    data class EMIBreakdown(
        val principal: Double,
        val interest: Double
    ) {
        val total: Double
            get() = principal + interest
    }
}

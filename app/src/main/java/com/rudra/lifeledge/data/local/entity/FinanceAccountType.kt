package com.rudra.lifeledge.data.local.entity

/**
 * Account types for double-entry accounting system.
 * 
 * In double-entry accounting:
 * - ASSET accounts: Increase with debits, decrease with credits (normal debit balance)
 * - LIABILITY accounts: Increase with credits, decrease with debits (normal credit balance)
 * - EQUITY accounts: Increase with credits, decrease with debits (normal credit balance)
 * - INCOME accounts: Increase with credits, decrease with debits (normal credit balance)
 * - EXPENSE accounts: Increase with debits, decrease with credits (normal debit balance)
 */
enum class FinanceAccountType {
    /** Assets represent resources owned by the user (cash, bank accounts, savings, etc.) */
    ASSET,
    
    /** Liabilities represent debts or obligations (loans, credit cards, etc.) */
    LIABILITY,
    
    /** Equity represents the owner's stake (net worth, retained earnings) */
    EQUITY,
    
    /** Income represents money received (salary, freelance, investments) */
    INCOME,
    
    /** Expenses represent money spent (food, transport, utilities, etc.) */
    EXPENSE
}

/**
 * Mapping from legacy AccountType to FinanceAccountType for backward compatibility.
 */
fun AccountType.toFinanceAccountType(): FinanceAccountType {
    return when (this) {
        AccountType.CASH -> FinanceAccountType.ASSET
        AccountType.BANK -> FinanceAccountType.ASSET
        AccountType.MOBILE_BANKING -> FinanceAccountType.ASSET
        AccountType.SAVINGS -> FinanceAccountType.ASSET
        AccountType.CREDIT_CARD -> FinanceAccountType.LIABILITY
    }
}

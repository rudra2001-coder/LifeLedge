package com.rudra.lifeledge.core.finance.engine

import com.rudra.lifeledge.data.local.dao.AccountDao
import com.rudra.lifeledge.data.local.dao.JournalLineDao
import com.rudra.lifeledge.data.local.dao.TransactionDao
import com.rudra.lifeledge.data.local.entity.Account
import com.rudra.lifeledge.data.local.entity.AccountType
import com.rudra.lifeledge.data.local.entity.TransactionType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*

/**
 * Unit tests for TransactionEngine
 */
class TransactionEngineTest {
    
    @Mock
    private lateinit var transactionDao: TransactionDao
    
    @Mock
    private lateinit var accountDao: AccountDao
    
    @Mock
    private lateinit var journalLineDao: JournalLineDao
    
    private lateinit var engine: TransactionEngine
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        engine = TransactionEngine(transactionDao, accountDao, journalLineDao)
    }
    
    @Test
    fun `validateTransaction should reject zero amount`() = kotlinx.coroutines.runBlocking {
        val result = engine.validateTransaction(
            amount = 0.0,
            fromAccountId = 1L,
            toAccountId = 2L,
            type = TransactionType.TRANSFER
        )
        
        assertTrue(result is TransactionResult.Error)
        assertEquals(
            TransactionErrorCode.INVALID_AMOUNT,
            (result as TransactionResult.Error).code
        )
    }
    
    @Test
    fun `validateTransaction should reject negative amount`() = kotlinx.coroutines.runBlocking {
        val result = engine.validateTransaction(
            amount = -100.0,
            fromAccountId = 1L,
            toAccountId = 2L,
            type = TransactionType.TRANSFER
        )
        
        assertTrue(result is TransactionResult.Error)
        assertEquals(
            TransactionErrorCode.INVALID_AMOUNT,
            (result as TransactionResult.Error).code
        )
    }
    
    @Test
    fun `validateTransaction should reject invalid source account`() = kotlinx.coroutines.runBlocking {
        whenever(accountDao.getAccount(1L)).thenReturn(null)
        
        val result = engine.validateTransaction(
            amount = 100.0,
            fromAccountId = 1L,
            toAccountId = 2L,
            type = TransactionType.TRANSFER
        )
        
        assertTrue(result is TransactionResult.Error)
        assertEquals(
            TransactionErrorCode.ACCOUNTS_NOT_FOUND,
            (result as TransactionResult.Error).code
        )
    }
    
    @Test
    fun `validateTransaction should reject invalid destination account`() = kotlinx.coroutines.runBlocking {
        val account = Account(2L, "Test", AccountType.CASH, 1000.0, "BDT", null, true, null)
        whenever(accountDao.getAccount(1L)).thenReturn(account)
        whenever(accountDao.getAccount(2L)).thenReturn(null)
        
        val result = engine.validateTransaction(
            amount = 100.0,
            fromAccountId = 1L,
            toAccountId = 2L,
            type = TransactionType.TRANSFER
        )
        
        assertTrue(result is TransactionResult.Error)
    }
    
    @Test
    fun `validateTransaction should reject insufficient balance for transfer`() = kotlinx.coroutines.runBlocking {
        val fromAccount = Account(1L, "Main", AccountType.CASH, 50.0, "BDT", null, true, null)
        val toAccount = Account(2L, "Savings", AccountType.SAVINGS, 0.0, "BDT", null, true, null)
        
        whenever(accountDao.getAccount(1L)).thenReturn(fromAccount)
        whenever(accountDao.getAccount(2L)).thenReturn(toAccount)
        whenever(journalLineDao.getAccountBalance(1L)).thenReturn(50.0)
        
        val result = engine.validateTransaction(
            amount = 100.0,
            fromAccountId = 1L,
            toAccountId = 2L,
            type = TransactionType.TRANSFER
        )
        
        assertTrue(result is TransactionResult.Error)
        assertEquals(
            TransactionErrorCode.INSUFFICIENT_BALANCE,
            (result as TransactionResult.Error).code
        )
    }
    
    @Test
    fun `validateTransaction should accept valid transfer with sufficient balance`() = kotlinx.coroutines.runBlocking {
        val fromAccount = Account(1L, "Main", AccountType.CASH, 200.0, "BDT", null, true, null)
        val toAccount = Account(2L, "Savings", AccountType.SAVINGS, 0.0, "BDT", null, true, null)
        
        whenever(accountDao.getAccount(1L)).thenReturn(fromAccount)
        whenever(accountDao.getAccount(2L)).thenReturn(toAccount)
        whenever(journalLineDao.getAccountBalance(1L)).thenReturn(200.0)
        
        val result = engine.validateTransaction(
            amount = 100.0,
            fromAccountId = 1L,
            toAccountId = 2L,
            type = TransactionType.TRANSFER
        )
        
        assertTrue(result is TransactionResult.Success)
    }
    
    @Test
    fun `calculateAccountBalance should return correct balance`() = kotlinx.coroutines.runBlocking {
        whenever(journalLineDao.getAccountBalance(1L)).thenReturn(500.0)
        
        val balance = engine.calculateAccountBalance(1L)
        
        assertEquals(500.0, balance, 0.01)
    }
    
    @Test
    fun `getAccountBalanceFlow should return flow of balance`() = kotlinx.coroutines.runBlocking {
        whenever(journalLineDao.getAccountBalanceFlow(1L)).thenReturn(flowOf(500.0))
        
        val balanceFlow = engine.getAccountBalanceFlow(1L)
        
        // Flow should emit 500.0
        val result = balanceFlow.first()
        assertEquals(500.0, result, 0.01)
    }
}

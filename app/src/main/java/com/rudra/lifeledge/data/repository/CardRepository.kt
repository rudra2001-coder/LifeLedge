package com.rudra.lifeledge.data.repository

import com.rudra.lifeledge.data.local.dao.CardDao
import com.rudra.lifeledge.data.local.entity.CardEntity
import kotlinx.coroutines.flow.Flow

class CardRepository(
    private val cardDao: CardDao
) {
    fun getAllCards(): Flow<List<CardEntity>> = cardDao.getAllCards()

    fun getAllCardsIncludingInactive(): Flow<List<CardEntity>> = cardDao.getAllCardsIncludingInactive()

    suspend fun getCard(id: Long): CardEntity? = cardDao.getCard(id)

    fun getTotalBalance(): Flow<Double?> = cardDao.getTotalBalance()

    suspend fun saveCard(card: CardEntity): Long = cardDao.insertCard(card)

    suspend fun updateCard(card: CardEntity) = cardDao.updateCard(card)

    suspend fun deleteCard(card: CardEntity) = cardDao.deleteCard(card)

    suspend fun updateBalance(cardId: Long, amount: Double) = cardDao.updateBalance(cardId, amount)

    suspend fun setActive(id: Long, isActive: Boolean) = cardDao.setActive(id, isActive)
}

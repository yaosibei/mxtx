package com.mindeye.app.feature.sos.data

import com.mindeye.app.core.database.dao.EmergencyContactDao
import com.mindeye.app.core.database.entity.EmergencyContactEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class EmergencyContactRepository(
    private val emergencyContactDao: EmergencyContactDao
) {

    suspend fun getAllContacts(): List<EmergencyContactEntity> {
        return emergencyContactDao.getAllContacts()
    }

    suspend fun getDefaultContacts(): List<EmergencyContactEntity> {
        return emergencyContactDao.getDefaultContacts()
    }

    suspend fun addContact(contact: EmergencyContactEntity) {
        emergencyContactDao.insertContact(contact)
    }

    suspend fun updateContact(contact: EmergencyContactEntity) {
        emergencyContactDao.updateContact(contact)
    }

    suspend fun deleteContact(contact: EmergencyContactEntity) {
        emergencyContactDao.deleteContact(contact)
    }

    fun observeContacts(): Flow<List<EmergencyContactEntity>> {
        return flow {
            val contacts = emergencyContactDao.getAllContacts()
            emit(contacts)
        }
    }
}

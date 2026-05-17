package com.mindeye.app.feature.sos.viewmodel

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.telephony.SmsManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindeye.app.core.database.entity.EmergencyContactEntity
import com.mindeye.app.feature.sos.data.EmergencyContactRepository
import com.mindeye.app.core.location.LocationService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EmergencyContactUiState(
    val contacts: List<EmergencyContactEntity> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class EmergencyViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val contactRepository: EmergencyContactRepository,
    private val locationService: LocationService
) : ViewModel() {

    private val _uiState = MutableStateFlow(EmergencyContactUiState())
    val uiState: StateFlow<EmergencyContactUiState> = _uiState.asStateFlow()

    init {
        loadContacts()
    }

    private fun loadContacts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val contacts = contactRepository.getAllContacts()
                _uiState.value = _uiState.value.copy(contacts = contacts, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message, isLoading = false)
            }
        }
    }

    fun callEmergency(number: String) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.CALL_PHONE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(context, "需要电话权限", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:$number")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun addContact(name: String, phone: String, relationship: String) {
        viewModelScope.launch {
            val contact = EmergencyContactEntity(
                id = System.currentTimeMillis().toString(),
                name = name,
                phone = phone,
                relationship = relationship,
                isDefault = false,
                sortOrder = (_uiState.value.contacts.size) + 1
            )
            contactRepository.addContact(contact)
            loadContacts()
        }
    }

    fun deleteContact(contact: EmergencyContactEntity) {
        viewModelScope.launch {
            contactRepository.deleteContact(contact)
            loadContacts()
        }
    }

    fun sendLocationMessage() {
        viewModelScope.launch {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.SEND_SMS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(context, "需要短信权限才能发送位置", Toast.LENGTH_LONG).show()
                return@launch
            }

            val contacts = _uiState.value.contacts
            if (contacts.isEmpty()) {
                Toast.makeText(context, "请先添加紧急联系人", Toast.LENGTH_LONG).show()
                return@launch
            }

            val location = locationService.getCurrentLocation()
            val locationText = if (location != null) {
                "https://maps.google.com/?q=${location.latitude},${location.longitude}"
            } else {
                "位置获取中，请稍后确认"
            }

            val message = "【明心同行紧急求助】我需要帮助！我的位置：${locationText}"
            val smsManager = context.getSystemService(android.telephony.SmsManager::class.java)
            var successCount = 0

            contacts.forEach { contact ->
                try {
                    if (message.length > 160) {
                        val parts = smsManager.divideMessage(message)
                        smsManager.sendMultipartTextMessage(contact.phone, null, parts, null, null)
                    } else {
                        smsManager.sendTextMessage(contact.phone, null, message, null, null)
                    }
                    successCount++
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            Toast.makeText(
                context,
                "已向 ${successCount}/${contacts.size} 位联系人发送求助短信",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

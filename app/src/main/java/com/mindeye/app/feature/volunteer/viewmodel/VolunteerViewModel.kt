package com.mindeye.app.feature.volunteer.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindeye.app.core.model.HelpRequestStatus
import com.mindeye.app.feature.community.data.HelpRequestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "VolunteerViewModel"

data class VolunteerUiState(
    val pendingRequests: List<com.mindeye.app.core.model.HelpRequest> = emptyList(),
    val myRequests: List<com.mindeye.app.core.model.HelpRequest> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val currentTab: VolunteerTab = VolunteerTab.PENDING,
    val stats: VolunteerStats = VolunteerStats()
)

data class VolunteerStats(
    val totalServed: Int = 0,
    val completedCount: Int = 0,
    val pendingCount: Int = 0,
    val rating: Float = 4.8f
)

enum class VolunteerTab {
    PENDING,    // 待接单
    MY_SERVICES // 我的服务
}

@HiltViewModel
class VolunteerViewModel @Inject constructor(
    private val helpRequestRepository: HelpRequestRepository
) : ViewModel() {

    private val currentUserId = "local_user"
    private val currentUserName = "我"

    private val _uiState = MutableStateFlow(VolunteerUiState())
    val uiState: StateFlow<VolunteerUiState> = _uiState.asStateFlow()

    init {
        observeDataChanges()
    }

    private fun observeDataChanges() {
        helpRequestRepository.getPendingRequestsFlow()
            .combine(helpRequestRepository.getVolunteerRequestsFlow(currentUserId)) { pending, my ->
                Pair(pending, my)
            }
            .onEach { (pendingRequests, myRequests) ->
                Log.d(TAG, "=== 数据更新 ===")
                Log.d(TAG, "待接单: ${pendingRequests.size} 条")
                Log.d(TAG, "我的服务: ${myRequests.size} 条")
                
                val completed = myRequests.count { it.status == HelpRequestStatus.COMPLETED }
                val pending = myRequests.count { it.status == HelpRequestStatus.ACCEPTED }
                
                _uiState.value = _uiState.value.copy(
                    pendingRequests = pendingRequests,
                    myRequests = myRequests,
                    stats = VolunteerStats(
                        totalServed = myRequests.size,
                        completedCount = completed,
                        pendingCount = pending,
                        rating = 4.8f
                    ),
                    isLoading = false
                )
                Log.d(TAG, "UI状态已更新")
            }
            .launchIn(viewModelScope)
    }

    fun loadData() {
        viewModelScope.launch {
            Log.d(TAG, "=== 强制刷新数据 ===")
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val pendingRequests = helpRequestRepository.getPendingRequests()
                Log.d(TAG, "查询到 ${pendingRequests.size} 条待接单求助")
                pendingRequests.forEach { req ->
                    Log.d(TAG, "求助: ID=${req.id}, 内容=${req.content.take(20)}, 状态=${req.status}")
                }
                
                val myRequests = helpRequestRepository.getVolunteerRequests(currentUserId)
                Log.d(TAG, "我的服务记录: ${myRequests.size} 条")
                
                _uiState.value = _uiState.value.copy(
                    pendingRequests = pendingRequests,
                    myRequests = myRequests,
                    isLoading = false
                )
            } catch (e: Exception) {
                Log.e(TAG, "加载数据失败", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun switchTab(tab: VolunteerTab) {
        _uiState.value = _uiState.value.copy(currentTab = tab)
    }

    fun acceptRequest(requestId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val success = helpRequestRepository.acceptRequest(
                    requestId = requestId,
                    volunteerId = currentUserId,
                    volunteerName = currentUserName
                )
                if (success) {
                    loadData()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun completeRequest(requestId: String) {
        viewModelScope.launch {
            try {
                val success = helpRequestRepository.completeRequest(requestId)
                if (success) {
                    loadData()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

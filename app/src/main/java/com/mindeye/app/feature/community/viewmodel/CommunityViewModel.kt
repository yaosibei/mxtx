package com.mindeye.app.feature.community.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindeye.app.core.model.*
import com.mindeye.app.core.database.dao.*
import com.mindeye.app.core.database.entity.*
import com.mindeye.app.feature.community.data.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

enum class VolunteerLevel(val level: Int, val label: String, val requiredCount: Int) {
    NEWBIE(1, "新手志愿者", 0),
    HELPER(2, "热心帮手", 5),
    EXPERT(3, "志愿达人", 20),
    MASTER(4, "志愿之星", 50),
    LEGEND(5, "志愿领袖", 100)
}

data class CommunityUiState(
    val posts: List<CommunityPost> = emptyList(),
    val helpRequests: List<CommunityPost> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isPosting: Boolean = false,
    val currentTab: CommunityTab = CommunityTab.ALL,
    val showHelpRequestDialog: Boolean = false,
    val showPostDialog: Boolean = false,
    val showMyRequests: Boolean = false,
    val isVolunteer: Boolean = false,
    val serviceCount: Int = 0,
    val volunteerLevel: VolunteerLevel = VolunteerLevel.NEWBIE,
    val badges: List<Badge> = emptyList(),
    val selectedCategory: HelpCategory? = null,
    val showCommentsFor: String? = null,
    val helpHistory: List<HelpRecord> = emptyList(),
    val showHelpHistory: Boolean = false,
    val showQuickTemplates: Boolean = false,
    val quickTemplates: List<QuickTemplate> = emptyList(),
    val volunteerRank: Int = 0,
    val volunteerStats: VolunteerStats = VolunteerStats(0, 0, 0, 0, 0.0),
    val showAchievementDialog: Boolean = false,
    val newlyUnlockedBadge: Badge? = null,
    val openRequestCount: Int = 0,
    val myRequestCount: Int = 0,
    val userRole: String = "user",
    val pendingOrders: List<VolunteerOrderItem> = emptyList(),
    val showPendingOrders: Boolean = false,
    val showVolunteerRanking: Boolean = false,
    val topVolunteers: List<VolunteerProfileEntity> = emptyList()
)

data class VolunteerOrderItem(
    val orderId: String,
    val postId: String,
    val requesterName: String,
    val content: String,
    val helpCategory: String,
    val createdAt: String,
    val status: String
)

data class HelpRecord(
    val postId: String,
    val requesterName: String,
    val content: String,
    val category: String,
    val status: String,
    val createdAt: String,
    val completedAt: String?
)

data class QuickTemplate(
    val title: String,
    val content: String,
    val category: HelpCategory,
    val urgency: UrgencyLevel
)

enum class CommunityTab { ALL, HELP_REQUESTS, MY_POSTS, MY_REQUESTS }

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val roleManager: UserRoleManager,
    private val volunteerProfileDao: VolunteerProfileDao,
    private val volunteerOrderDao: VolunteerOrderDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommunityUiState(
        quickTemplates = listOf(
            QuickTemplate("导航到地铁站", "我现在在{位置}，需要志愿者引导到最近的地铁站", HelpCategory.NAVIGATION, UrgencyLevel.MEDIUM),
            QuickTemplate("帮忙拎东西", "我在{位置}，买的东西太多需要帮忙拎回家", HelpCategory.CARRY_ITEMS, UrgencyLevel.LOW),
            QuickTemplate("找路求助", "我在{位置}，找不到路了需要志愿者帮忙", HelpCategory.NAVIGATION, UrgencyLevel.HIGH),
            QuickTemplate("陪伴出行", "我想去{目的地}，需要志愿者陪同出行", HelpCategory.COMPANION, UrgencyLevel.LOW),
            QuickTemplate("紧急求助", "我在{位置}，遇到紧急情况需要志愿者帮助！", HelpCategory.EMERGENCY, UrgencyLevel.HIGH)
        )
    ))
    val uiState: StateFlow<CommunityUiState> = _uiState.asStateFlow()

    init {
        try {
            loadInitialRole()
            loadPosts()
            loadHelpRequests()
            loadCounts()
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(errorMessage = e.message)
        }
    }

    private fun loadInitialRole() {
        viewModelScope.launch {
            try {
                val role = roleManager.getUserRole()
                updateRoleState(role)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "加载角色失败: ${e.message}")
            }
        }
    }

    private fun updateRoleState(role: String) {
        viewModelScope.launch {
            try {
                val volunteer = role == UserRoleManager.ROLE_VOLUNTEER
                val count = roleManager.getServiceCount()
                val badges = roleManager.getBadges()
                val level = getVolunteerLevel(count)

                _uiState.value = _uiState.value.copy(
                    isVolunteer = volunteer,
                    userRole = role,
                    serviceCount = count,
                    badges = badges,
                    volunteerLevel = level,
                    showHelpHistory = false,
                    showPendingOrders = false,
                    showVolunteerRanking = false
                )

                if (volunteer) {
                    loadVolunteerStats("volunteer_1")
                    loadPendingOrders("volunteer_1")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "加载角色信息失败: ${e.message}")
            }
        }
    }

    fun refreshRoleState() {
        viewModelScope.launch {
            val role = roleManager.getUserRole()
            updateRoleState(role)
            loadCounts()
        }
    }

    private fun getVolunteerLevel(count: Int): VolunteerLevel {
        return VolunteerLevel.values().reversed().firstOrNull { it.requiredCount <= count } ?: VolunteerLevel.NEWBIE
    }

    private fun loadCounts() {
        viewModelScope.launch {
            try {
                val openCount = postRepository.getOpenHelpRequestCount()
                val myCount = postRepository.getUserHelpRequestCount("local_user")
                _uiState.value = _uiState.value.copy(
                    openRequestCount = openCount,
                    myRequestCount = myCount
                )
            } catch (_: Exception) {}
        }
    }

    private fun loadVolunteerStats(volunteerId: String) {
        viewModelScope.launch {
            try {
                val stats = postRepository.getVolunteerStats(volunteerId)
                _uiState.value = _uiState.value.copy(volunteerStats = stats)
            } catch (_: Exception) {}
        }
    }

    private fun loadPendingOrders(volunteerId: String) {
        viewModelScope.launch {
            try {
                val orders = volunteerOrderDao.getPendingOrders().map {
                    VolunteerOrderItem(
                        orderId = it.orderId,
                        postId = it.postId,
                        requesterName = it.requesterName,
                        content = it.content,
                        helpCategory = it.helpCategory,
                        createdAt = formatTimestamp(it.createdAt),
                        status = it.status
                    )
                }
                _uiState.value = _uiState.value.copy(pendingOrders = orders)
            } catch (_: Exception) {}
        }
    }

    fun loadPosts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val posts = postRepository.getRegularPosts()
                if (posts.isEmpty()) seedSamplePosts()
                _uiState.value = _uiState.value.copy(
                    posts = postRepository.getRegularPosts(),
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message, isLoading = false)
            }
        }
    }

    fun loadHelpRequests() {
        viewModelScope.launch {
            try {
                val filtered = _uiState.value.selectedCategory?.let { cat ->
                    postRepository.getHelpRequestsByCategory(cat)
                } ?: postRepository.getHelpRequests()
                _uiState.value = _uiState.value.copy(helpRequests = filtered)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun loadMyRequests() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val myRequests = postRepository.getUserHelpRequests("local_user")
                _uiState.value = _uiState.value.copy(helpRequests = myRequests, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message, isLoading = false)
            }
        }
    }

    fun switchTab(tab: CommunityTab) {
        _uiState.value = _uiState.value.copy(
            currentTab = tab,
            showHelpHistory = false,
            showPendingOrders = false,
            showVolunteerRanking = false
        )
        when (tab) {
            CommunityTab.ALL -> loadPosts()
            CommunityTab.HELP_REQUESTS -> loadHelpRequests()
            CommunityTab.MY_POSTS -> {}
            CommunityTab.MY_REQUESTS -> loadMyRequests()
        }
    }

    fun filterByCategory(category: HelpCategory?) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        loadHelpRequests()
    }

    fun createPost(userId: String, userName: String, content: String, postType: PostType) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isPosting = true)
            try {
                postRepository.createPost(userId, userName, content, postType)
                loadPosts()
            } catch (e: Exception) { _uiState.value = _uiState.value.copy(errorMessage = e.message) }
            _uiState.value = _uiState.value.copy(isPosting = false, showPostDialog = false)
        }
    }

    fun createHelpRequest(
        userId: String, userName: String, content: String,
        helpCategory: HelpCategory = HelpCategory.OTHER,
        urgencyLevel: UrgencyLevel = UrgencyLevel.LOW,
        latitude: Double? = null, longitude: Double? = null, locationName: String? = null
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isPosting = true)
            try {
                postRepository.createHelpRequest(userId, userName, content, helpCategory, urgencyLevel, latitude, longitude, locationName)
                loadHelpRequests()
                loadCounts()
            } catch (e: Exception) { _uiState.value = _uiState.value.copy(errorMessage = e.message) }
            _uiState.value = _uiState.value.copy(isPosting = false, showHelpRequestDialog = false)
        }
    }

    fun useTemplate(template: QuickTemplate) {
        _uiState.value = _uiState.value.copy(showHelpRequestDialog = true, showQuickTemplates = false)
    }

    fun likePost(postId: String) {
        viewModelScope.launch {
            try {
                postRepository.likePost(postId)
                loadPosts()
                loadHelpRequests()
            } catch (e: Exception) { _uiState.value = _uiState.value.copy(errorMessage = e.message) }
        }
    }

    fun addComment(postId: String, authorName: String, content: String) {
        viewModelScope.launch {
            try {
                postRepository.addComment(postId, authorName, content, _uiState.value.isVolunteer)
                loadPosts()
                loadHelpRequests()
                _uiState.value = _uiState.value.copy(showCommentsFor = null)
            } catch (e: Exception) { _uiState.value = _uiState.value.copy(errorMessage = e.message) }
        }
    }

    fun toggleComments(postId: String?) {
        _uiState.value = _uiState.value.copy(showCommentsFor = if (_uiState.value.showCommentsFor == postId) null else postId)
    }

    fun acceptHelpRequest(postId: String, volunteerId: String, volunteerName: String) {
        viewModelScope.launch {
            try {
                postRepository.acceptHelpRequest(postId, volunteerId, volunteerName)
                val newCount = roleManager.incrementServiceCount()
                val badges = roleManager.getBadges()
                val level = getVolunteerLevel(newCount)

                val previousCount = newCount - 1
                val newlyUnlocked = badges.find { badge ->
                    when (badge.id) {
                        "first_help" -> previousCount == 0 && newCount == 1
                        "helper" -> previousCount < 5 && newCount >= 5
                        "expert" -> previousCount < 20 && newCount >= 20
                        else -> false
                    }
                }

                loadHelpRequests()
                loadCounts()
                loadVolunteerStats(volunteerId)
                loadPendingOrders(volunteerId)

                _uiState.value = _uiState.value.copy(
                    serviceCount = newCount,
                    badges = badges,
                    volunteerLevel = level,
                    newlyUnlockedBadge = newlyUnlocked,
                    showAchievementDialog = newlyUnlocked != null
                )
            } catch (e: Exception) { _uiState.value = _uiState.value.copy(errorMessage = e.message) }
        }
    }

    fun completeHelpRequest(postId: String) {
        viewModelScope.launch {
            try {
                postRepository.completeHelpRequest(postId, "volunteer_1")
                loadHelpRequests()
                loadCounts()
                loadVolunteerStats("volunteer_1")
                loadPendingOrders("volunteer_1")
            } catch (e: Exception) { _uiState.value = _uiState.value.copy(errorMessage = e.message) }
        }
    }

    fun rateHelpRequest(postId: String, orderId: String, rating: Int, feedback: String?) {
        viewModelScope.launch {
            try {
                postRepository.rateHelpRequest(postId, orderId, "local_user", rating, feedback)
                loadMyRequests()
            } catch (e: Exception) { _uiState.value = _uiState.value.copy(errorMessage = e.message) }
        }
    }

    fun toggleHelpHistory() {
        viewModelScope.launch {
            val currentState = _uiState.value.showHelpHistory
            if (!currentState) {
                val history = roleManager.getHelpHistory()
                val formattedHistory = history.map { record ->
                    HelpRecord(
                        postId = record.postId,
                        requesterName = record.requesterName,
                        content = record.content,
                        category = record.helpCategory,
                        status = record.status,
                        createdAt = formatTimestamp(record.createdAt),
                        completedAt = record.completedAt?.let { formatTimestamp(it) }
                    )
                }
                _uiState.value = _uiState.value.copy(
                    showHelpHistory = true,
                    helpHistory = formattedHistory
                )
            } else {
                _uiState.value = _uiState.value.copy(showHelpHistory = false)
            }
        }
    }

    fun togglePendingOrders() {
        val current = _uiState.value.showPendingOrders
        _uiState.value = _uiState.value.copy(showPendingOrders = !current)
        if (!current) {
            loadPendingOrders("volunteer_1")
        }
    }

    fun toggleVolunteerRanking() {
        val current = _uiState.value.showVolunteerRanking
        _uiState.value = _uiState.value.copy(showVolunteerRanking = !current)
        if (!current) {
            viewModelScope.launch {
                try {
                    val top = postRepository.getTopVolunteers(10)
                    _uiState.value = _uiState.value.copy(topVolunteers = top)
                } catch (_: Exception) {}
            }
        }
    }

    fun toggleQuickTemplates() {
        _uiState.value = _uiState.value.copy(showQuickTemplates = !_uiState.value.showQuickTemplates)
    }

    fun dismissAchievementDialog() {
        _uiState.value = _uiState.value.copy(showAchievementDialog = false, newlyUnlockedBadge = null)
    }

    fun showHelpRequestDialog(show: Boolean) { _uiState.value = _uiState.value.copy(showHelpRequestDialog = show) }
    fun showPostDialog(show: Boolean) { _uiState.value = _uiState.value.copy(showPostDialog = show) }
    fun clearError() { _uiState.value = _uiState.value.copy(errorMessage = null) }

    private suspend fun seedSamplePosts() {
        postRepository.createPost("user1", "小明", "今天第一次用明心同行出门，场景分析真的很准确！震动提醒帮我避开了很多障碍。", PostType.TEXT)
        postRepository.createPost("user2", "小红", "请问大家平时是怎么使用语音输入功能的？有没有什么小技巧可以分享？", PostType.TEXT)
        postRepository.createHelpRequest("user3", "老李", "我现在在人民公园附近，需要志愿者帮忙引导到地铁站，大约需要10分钟。", HelpCategory.NAVIGATION, UrgencyLevel.MEDIUM, latitude = 39.9042, longitude = 116.4074, locationName = "人民公园")
        postRepository.createHelpRequest("user4", "王阿姨", "我在超市买完东西，需要帮忙拎东西回家，我在朝阳路超市门口。", HelpCategory.CARRY_ITEMS, UrgencyLevel.LOW, latitude = 39.9142, longitude = 116.4174, locationName = "朝阳路超市")
        postRepository.createHelpRequest("user5", "张大爷", "我迷路了，需要志愿者帮忙找一下回家的路。", HelpCategory.EMERGENCY, UrgencyLevel.HIGH, locationName = "未知位置")
    }

    private fun formatTimestamp(ts: Long): String {
        val sdf = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
        return sdf.format(Date(ts))
    }
}
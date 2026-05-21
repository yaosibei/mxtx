package com.mindeye.app.feature.community.data

import com.mindeye.app.core.database.dao.EncouragementDao
import com.mindeye.app.core.database.dao.SceneRecordDao
import com.mindeye.app.core.database.entity.EncouragementEntity
import com.mindeye.app.core.model.Encouragement

class EncouragementRepository(
    private val encouragementDao: EncouragementDao,
    private val sceneRecordDao: SceneRecordDao
) {

    suspend fun getUserEncouragements(userId: String, limit: Int = 20): List<Encouragement> {
        return encouragementDao.getUserEncouragements(userId, limit).map { it.toDomainModel() }
    }

    suspend fun getUnreadEncouragements(userId: String): List<Encouragement> {
        return encouragementDao.getUnreadEncouragements(userId).map { it.toDomainModel() }
    }

    suspend fun markAsRead(id: String) {
        encouragementDao.markAsRead(id)
    }

    /**
     * 根据出行记录（场景记录）生成鼓励语
     */
    suspend fun generateEncouragementFromTravel(userId: String): Encouragement? {
        val recentRecords = sceneRecordDao.getRecentRecords(10)
        if (recentRecords.isEmpty()) return null

        // 分析最近的出行记录
        val hasIndoorQuiet = recentRecords.any { it.sceneType == "indoor" && it.sceneSubtype == "quiet" }
        val hasOutdoor = recentRecords.any { it.sceneType == "outdoor" }
        val hasSocial = recentRecords.any { it.sceneType == "social" }
        val recordCount = recentRecords.size

        val encouragementText = when {
            hasIndoorQuiet -> {
                // 室内安静场景 - 适合给予安静的鼓励
                val messages = listOf(
                    "安静的环境是最好的自我成长空间，你已经在探索中前进了一步，继续加油！",
                    "在安静的房间里，可以好好规划接下来的行程，你做得很好。",
                    "安静的环境让心灵更平静，这是调整状态的好时机。"
                )
                messages.random()
            }
            hasOutdoor -> {
                // 户外出行 - 鼓励勇敢出行
                val messages = listOf(
                    "每一次出行都是一次勇敢的尝试，你已经在不断突破自己，很棒！",
                    "户外的世界很精彩，你能够独立出行，真的非常了不起。",
                    "走出去就是进步，注意安全，明心同行会一直陪伴你。"
                )
                messages.random()
            }
            hasSocial -> {
                // 社交场景 - 鼓励社交互动
                val messages = listOf(
                    "与他人交流是很好的放松方式，你在社交中表现得很棒！",
                    "走出家门，和朋友一起聊天，这样的生活充实而美好。",
                    "社交是建立连接的方式，你的每一次参与都在丰富自己的人生。"
                )
                messages.random()
            }
            recordCount >= 5 -> {
                // 出行记录较多
                val messages = listOf(
                    "今天你出了很多次门呢，生活很充实！继续保持这份积极性。",
                    "多次出行说明你在积极面对生活，为你点赞！"
                )
                messages.random()
            }
            else -> {
                val messages = listOf(
                    "每一天都是新的开始，不管怎样，你都在努力生活，这本身就值得鼓励。",
                    "无论今天做了什么，都值得被肯定。明天会更好！",
                    "生活偶尔有挑战，但你一直勇敢地面对，真的很棒。"
                )
                messages.random()
            }
        }

        val triggerScene = when {
            hasIndoorQuiet -> "indoor_quiet"
            hasOutdoor -> "outdoor_travel"
            hasSocial -> "social_interaction"
            else -> "general"
        }

        val encouragement = EncouragementEntity(
            id = System.currentTimeMillis().toString(),
            userId = userId,
            content = encouragementText,
            triggerScene = triggerScene
        )
        encouragementDao.insertEncouragement(encouragement)

        return encouragement.toDomainModel()
    }

    suspend fun cleanupOldEncouragements() {
        val thresholdTime = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        encouragementDao.deleteOldEncouragements(thresholdTime)
    }

    private fun EncouragementEntity.toDomainModel(): Encouragement {
        return Encouragement(
            id = id,
            content = content,
            triggerScene = triggerScene,
            timestamp = createdAt
        )
    }
}

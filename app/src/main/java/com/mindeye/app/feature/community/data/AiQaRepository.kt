package com.mindeye.app.feature.community.data

import com.mindeye.app.core.database.dao.AiQaDao
import com.mindeye.app.core.database.entity.AiQaEntity
import com.mindeye.app.core.model.AiQaMessage

class AiQaRepository(
    private val aiQaDao: AiQaDao
) {

    suspend fun getQaHistory(userId: String, limit: Int = 50): List<AiQaMessage> {
        val entities = aiQaDao.getUserQaHistory(userId, limit)
        val messages = mutableListOf<AiQaMessage>()
        entities.forEach { entity ->
            messages.add(
                AiQaMessage(
                    id = entity.id * 2 - 1,
                    content = entity.question,
                    isFromUser = true,
                    timestamp = entity.timestamp
                )
            )
            messages.add(
                AiQaMessage(
                    id = entity.id * 2,
                    content = entity.answer,
                    isFromUser = false,
                    timestamp = entity.timestamp + 1
                )
            )
        }
        return messages.reversed()
    }

    suspend fun saveQa(userId: String, question: String, answer: String) {
        val entity = AiQaEntity(
            userId = userId,
            question = question,
            answer = answer
        )
        aiQaDao.insertQa(entity)
    }

    suspend fun cleanupOldQa() {
        val thresholdTime = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        aiQaDao.deleteOldQa(thresholdTime)
    }

    fun generateAiAnswer(question: String): String {
        val lowerMsg = question.lowercase()

        // 社区求助相关
        val communityAnswers = when {
            lowerMsg.contains("求助") || lowerMsg.contains("帮忙") -> listOf(
                "你可以在社区页面发布求助信息，附近的志愿者会尽快接单帮助你。点击右上角的发布求助按钮即可。",
                "发布求助时建议写明具体需求和位置信息，这样志愿者能更快响应。"
            )
            lowerMsg.contains("志愿者") || lowerMsg.contains("接单") -> listOf(
                "志愿者可以在社区页面看到待接单的求助请求，点击接单即可提供帮助。感谢你的热心参与！",
                "成为志愿者可以帮助更多需要帮助的人，系统会优先推送附近的求助信息给你。"
            )
            lowerMsg.contains("怎么") || lowerMsg.contains("如何") -> listOf(
                "有什么问题可以详细告诉我，我会尽力帮助你。",
                "你可以描述一下具体遇到的困难，我会给你更针对性的建议。"
            )
            lowerMsg.contains("谢谢") || lowerMsg.contains("感谢") -> listOf(
                "不客气，能帮助到你我很开心。如果还有其他问题随时问我。",
                "你的感谢是对我们最大的鼓励，祝你生活愉快！"
            )
            lowerMsg.contains("心情") || lowerMsg.contains("情绪") || lowerMsg.contains("难过") -> listOf(
                "情绪低落是很正常的，你可以试试心理支持功能，和我聊聊天，或者在社区里和其他朋友交流。",
                "记住你不是一个人，有很多人关心你。深呼吸，慢慢来，一切都会好起来的。"
            )
            lowerMsg.contains("出行") || lowerMsg.contains("导航") || lowerMsg.contains("路") -> listOf(
                "出行时可以使用明心同行导航功能，它会为你提供语音引导和障碍提醒。",
                "建议出行前先使用明心之眼检查周围环境，确保安全后再出发。"
            )
            lowerMsg.contains("安全") || lowerMsg.contains("危险") -> listOf(
                "安全是第一位的。遇到紧急情况可以使用SOS功能，系统会立即通知你的紧急联系人。",
                "夜间出行建议提前告知家人朋友，并保持手机电量充足。"
            )
            lowerMsg.contains("社区") || lowerMsg.contains("帖子") -> listOf(
                "社区是大家互相帮助交流的地方，你可以发布求助、分享经验，也可以帮助别人。",
                "在社区里可以看到其他用户发布的求助信息，你也可以参与评论和点赞。"
            )
            else -> listOf(
                "我在听你说，请继续描述你的问题，我会尽力帮助你。",
                "你的问题很重要，让我想想怎么帮你解决。",
                "有什么困难都可以告诉我，我们一起想办法。"
            )
        }

        return communityAnswers.random()
    }
}

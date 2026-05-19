package com.mindeye.app.core.model

import com.mindeye.app.feature.psychology.data.TravelRecordRepository
import javax.inject.Inject
import javax.inject.Singleton

data class EncouragementResult(
    val message: String,
    val travelCount: Int,
    val totalDistance: Double,
    val totalDurationMinutes: Long
)

@Singleton
class GenerateEncouragementUseCase @Inject constructor(
    private val travelRecordRepository: TravelRecordRepository
) {
    suspend fun execute(userId: String): EncouragementResult {
        val count = travelRecordRepository.getUserRecordCount(userId)
        val distance = travelRecordRepository.getUserTotalDistance(userId)
        val durationMs = travelRecordRepository.getUserTotalDuration(userId)
        val durationMinutes = durationMs / (1000 * 60)

        val message = when {
            count == 0 -> "今天还没有出行记录，勇敢出门走走吧，每一步都是进步！"
            count == 1 -> "今天完成了1次出行，你很棒！继续加油，世界等着你去探索。"
            count <= 3 -> "今天已经出行了${count}次，累计${String.format("%.1f", distance / 1000)}公里。你的勇气令人敬佩！"
            count <= 5 -> "太厉害了！今天${count}次出行，累计${String.format("%.1f", distance / 1000)}公里。你是勇敢的探索者！"
            else -> "惊人的${count}次出行！累计${String.format("%.1f", distance / 1000)}公里，${durationMinutes}分钟。你的坚持让人感动！"
        }

        return EncouragementResult(
            message = message,
            travelCount = count,
            totalDistance = distance,
            totalDurationMinutes = durationMinutes
        )
    }
}

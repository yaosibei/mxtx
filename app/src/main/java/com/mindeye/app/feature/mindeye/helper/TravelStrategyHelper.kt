package com.mindeye.app.feature.mindeye.helper

object TravelStrategyHelper {

    fun generatePlan(destination: String): String {
        val target = destination.trim()

        return when {
            target.contains("医院") -> {
                "即将前往医院，建议提前准备好身份证和挂号信息，并留意门诊入口与无障碍通道"
            }

            target.contains("高铁") || target.contains("火车站") || target.contains("车站") -> {
                "即将前往车站，人流较大，已调高避障灵敏度，建议提前确认进站口和候车区域"
            }

            target.contains("机场") -> {
                "即将前往机场，建议提前确认航站楼信息，并准备好身份证件与出行订单"
            }

            target.contains("学校") || target.contains("大学") || target.contains("学院") -> {
                "即将前往学校，校园道路较长，建议注意岔路口、台阶和人群聚集区域"
            }

            target.contains("银行") -> {
                "即将前往银行，建议提前确认办理业务类型，并留意叫号区和柜台位置"
            }

            else -> {
                "已为您生成通用出行建议，请注意前方环境变化，保持手机朝前，必要时随时发起障碍识别"
            }
        }
    }
}

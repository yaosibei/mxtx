# 文件夹说明

```text
MingXinTongXing/
  app/
    src/main/java/com/mindeye/app/
      app/                  App 入口、导航、依赖注入
      core/                 全项目公共能力
        database/           Room 数据库
        feedback/           TTS、震动、统一反馈
        location/           定位、路线相关基础能力
        model/              公共数据模型和通用 UseCase
        network/            后端 API、AI API
        sensor/             环境音、运动传感器
        ui/                 全局 UI 组件，比如 SOS 按钮
        worker/             后台任务
      feature/              业务功能模块
        home/               首页
        mindeye/            明心之眼：出行前检查、出行陪伴、快速识别
        senseflow/          随境 SenseFlow 后台环境感知
        community/          社区互助、求助、志愿者
        psychology/         心理支持、AI 问答、鼓励语
        sos/                SOS 紧急求助
        settings/           长辈模式、设置
      ui/                   MainActivity 和主题
```

## 被删掉的旧代码

这些旧代码和当前 Compose 主流程不匹配，所以没有放进新版本：

```text
CommunityFragment.kt
EmergencyFragment.kt
SupportFragment.kt
activity_main.xml
fragment_community.xml
fragment_emergency.xml
fragment_support.xml
```

原因：当前 App 已经使用 Compose 页面，继续保留旧 Fragment/XML 会让成员分工混乱。

# 六人分工对应文件夹

## 成员 1：项目负责人 + Android 架构

负责文件夹：

```text
app/src/main/java/com/mindeye/app/app/
app/src/main/java/com/mindeye/app/core/database/
app/src/main/java/com/mindeye/app/core/network/
app/src/main/java/com/mindeye/app/core/model/
```

主要任务：

- App 入口
- Navigation 路由
- Hilt 依赖注入
- Room 数据库
- 网络接口基础类
- 每周代码集成
- 最终打包

---

## 成员 2：明心之眼 / 视觉识别

负责文件夹：

```text
app/src/main/java/com/mindeye/app/feature/mindeye/vision/
app/src/main/java/com/mindeye/app/feature/mindeye/ui/OcrScreen.kt
app/src/main/java/com/mindeye/app/feature/mindeye/ui/MindEyeTravelScreens.kt
app/src/main/java/com/mindeye/app/feature/mindeye/domain/model/
```

主要任务：

- CameraX 摄像头
- OCR 文字识别
- 物体识别
- 障碍识别
- 出行前环境检查
- 问一下 / 快速识别

---

## 成员 3：语音交互 + 随境 SenseFlow

负责文件夹：

```text
app/src/main/java/com/mindeye/app/core/feedback/
app/src/main/java/com/mindeye/app/core/sensor/
app/src/main/java/com/mindeye/app/feature/senseflow/
```

主要任务：

- TTS 播报
- 震动提醒
- 后续接入 STT 语音输入
- 环境音检测
- 运动状态检测
- SenseFlow 场景判断
- 不同环境下的提醒策略

---

## 成员 4：出行陪伴 + 路线 + 一键回家

负责文件夹：

```text
app/src/main/java/com/mindeye/app/core/location/
app/src/main/java/com/mindeye/app/feature/mindeye/travel/
app/src/main/java/com/mindeye/app/feature/mindeye/ui/MindEyeTravelScreens.kt
```

主要任务：

- 定位
- 地图 SDK
- 一键回家
- 转弯提醒
- 偏航提醒
- 出行方案生成
- 高铁站/机场/医院提前服务提示

---

## 成员 5：社区互动 + 后端

负责文件夹：

```text
app/src/main/java/com/mindeye/app/feature/community/
app/src/main/java/com/mindeye/app/core/network/
```

主要任务：

- 用户登录
- 社区帖子
- 求助发布
- 志愿者接单
- 位置上传接口
- 后端 API
- 数据库表设计

---

## 成员 6：心理支持 + 适老化 UI + 测试文档

负责文件夹：

```text
app/src/main/java/com/mindeye/app/feature/psychology/
app/src/main/java/com/mindeye/app/feature/settings/
app/src/main/java/com/mindeye/app/core/ui/
```

主要任务：

- 心理支持页面
- AI 问答接口
- 出行记录分析
- 室内安静时生成鼓励语
- 长辈模式
- 大字号 / 高对比度
- TalkBack 测试
- 演示脚本

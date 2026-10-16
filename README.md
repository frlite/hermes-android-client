# Hermes Android Client

原生安卓客户端，基于 Kotlin + Jetpack Compose。

## 功能

- 基本对话（OpenAI 兼容 API）
- 会话保存（本地 JSON 存储）
- 多会话管理（新建、切换、删除）
- 设置页面（配置 API Key 和 Base URL）

## 构建

### 前置要求

- Android Studio Hedgehog (2023.1) 或更高版本
- JDK 17
- Android SDK 34

### 步骤

1. 克隆仓库
   ```bash
   git clone https://github.com/frlite/hermes-android-client.git
   cd hermes-android-client
   ```

2. 用 Android Studio 打开项目，或命令行构建：
   ```bash
   ./gradlew assembleDebug
   ```

3. APK 输出路径：`app/build/outputs/apk/debug/app-debug.apk`

## 配置

1. 安装应用后，点击右上角设置图标
2. 填入 API Base URL（默认 OpenRouter）和 API Key
3. 点击保存

## 技术栈

- Kotlin + Jetpack Compose
- Material 3
- OkHttp（网络请求）
- Gson（JSON 序列化）
- Navigation Compose

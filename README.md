# X 单次关注助手

一个最小 Android 应用：用户主动输入一个 X 用户名/主页链接后，应用在 60 秒内授权无障碍服务在官方 X App (`com.twitter.android`) 中尝试点击一次精确文本为 `Follow` 或 `关注` 的可点击控件。成功后立即解除本次授权。

## 特点
- 不保存 X 密码或 Cookie
- 不做账号批量导入
- 不循环关注
- 每次必须从主界面手动触发
- 60 秒后自动失效
- 成功点击一次后立即失效

## 不用 Android Studio：GitHub 自动生成 APK

仓库已经包含 `.github/workflows/build-apk.yml`。

1. 打开仓库的 **Actions** 页面。
2. 选择 **Build Android APK**。
3. 如果没有自动开始，点 **Run workflow**。
4. 构建完成后，在该次运行页面的 **Artifacts** 下载 `XFollowHelper-debug-apk`。
5. 解压后得到 `app-debug.apk`，发送到 Android 手机并安装。

这是 debug APK，Android 可能提示“未知来源应用”，需要你在系统里允许浏览器/文件管理器安装该 APK。

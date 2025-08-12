# VSCode Spring Boot 热部署指南

## 🎯 概述

由于VSCode不像IntelliJ IDEA那样有内置的自动编译功能，我们需要使用其他方式来实现热部署。我为您提供了3种解决方案。

## 🚀 方案1：自动文件监控（推荐）

### 安装依赖
```bash
# 安装Node.js依赖
npm install

# 或者手动安装
npm install -g concurrently
```

### 使用方法
```bash
# 方式1：一键启动（推荐）
npm run dev

# 方式2：分别启动
# 终端1：启动文件监控
npm run watch

# 终端2：启动Spring Boot应用
mvn spring-boot:run -f jeecg-module-system/jeecg-system-start/pom.xml -Dspring.profiles.active=dev
```

### 工作原理
1. `watch-and-compile.js` 监控Java文件和配置文件变化
2. 检测到变化后自动执行 `mvn compile`
3. DevTools检测到class文件变化，自动重启应用

## 🔧 方案2：VSCode任务（手动触发）

### 使用方法
1. 按 `Ctrl+Shift+P` 打开命令面板
2. 输入 `Tasks: Run Task`
3. 选择以下任务之一：
   - `compile-energy-module`：编译能源模块
   - `hot-deploy-compile`：静默编译（用于热部署）
   - `start-spring-boot-dev`：启动开发服务器

### 快捷键设置
在VSCode中设置快捷键来快速编译：

1. 按 `Ctrl+Shift+P`，输入 `Preferences: Open Keyboard Shortcuts (JSON)`
2. 添加以下配置：
```json
[
    {
        "key": "ctrl+f9",
        "command": "workbench.action.tasks.runTask",
        "args": "hot-deploy-compile"
    }
]
```

## 🎮 方案3：VSCode调试启动

### 使用方法
1. 按 `F5` 或点击调试按钮
2. 选择 `Spring Boot App (Dev)` 配置
3. 应用将以开发模式启动，支持热部署

### 修改代码后
- 按 `Ctrl+F9` 快速编译
- 或者运行任务：`hot-deploy-compile`

## 📋 已配置的文件

### `.vscode/settings.json`
- 启用Java自动构建
- 配置Maven自动更新
- 优化文件监控

### `.vscode/tasks.json`
- 编译任务配置
- Spring Boot启动任务
- 问题匹配器配置

### `.vscode/launch.json`
- 调试配置
- 开发环境参数
- DevTools启用

### `watch-and-compile.js`
- 文件监控脚本
- 自动编译触发
- 防抖处理

### `package.json`
- npm脚本配置
- 依赖管理

## 🧪 测试热部署

### 步骤1：启动应用
```bash
npm run dev
```

### 步骤2：测试接口
访问：`http://localhost:8080/jeecg-boot/energy/realtime/test/hotdeploy`

### 步骤3：修改代码
修改 `RealtimeTestController.java` 中的 `testHotDeploy` 方法：
```java
@GetMapping("/hotdeploy")
public Result<String> testHotDeploy() {
    String timestamp = new Date().toString();
    log.info("🔥 热部署测试 v2.0 - 当前时间: {}", timestamp);
    return Result.OK("热部署测试成功！版本 v2.0 - 当前时间: " + timestamp);
}
```

### 步骤4：观察效果
- 保存文件后，监控脚本会自动编译
- 控制台显示编译信息
- DevTools自动重启应用
- 再次访问接口，确认修改生效

## 📊 性能对比

| 方案 | 自动化程度 | 响应速度 | 配置复杂度 |
|------|------------|----------|------------|
| 方案1（文件监控） | 全自动 | 3-8秒 | 简单 |
| 方案2（手动任务） | 半自动 | 2-5秒 | 简单 |
| 方案3（调试模式） | 半自动 | 2-5秒 | 简单 |

## 🐛 故障排除

### 问题1：文件监控不工作
**解决方案**：
1. 确保安装了Node.js
2. 检查 `watch-and-compile.js` 中的路径
3. 确认文件权限

### 问题2：编译失败
**解决方案**：
1. 检查Java语法错误
2. 确认Maven配置正确
3. 查看编译错误信息

### 问题3：DevTools不重启
**解决方案**：
1. 确认 `spring.profiles.active=dev`
2. 检查 `application-dev.yml` 配置
3. 确认DevTools依赖已添加

### 问题4：端口冲突
**解决方案**：
1. 检查8080端口是否被占用
2. 修改 `application-dev.yml` 中的端口
3. 或者停止占用端口的进程

## 💡 最佳实践

1. **推荐使用方案1**：自动化程度最高，开发体验最好
2. **保持文件结构清晰**：避免深层嵌套影响监控
3. **合理使用防抖**：避免频繁编译影响性能
4. **关注控制台输出**：及时发现编译错误
5. **定期清理target目录**：避免缓存问题

## 🎉 总结

虽然VSCode不能像IntelliJ IDEA那样原生支持热部署，但通过以上配置，您可以获得接近的开发体验：

- ✅ 自动文件监控
- ✅ 快速编译
- ✅ 自动重启
- ✅ 3-8秒响应时间

现在您可以在VSCode中享受高效的Spring Boot开发了！🚀

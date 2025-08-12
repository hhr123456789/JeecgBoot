@echo off
chcp 65001
echo 🚀 启动Spring Boot开发环境（支持热部署）

echo 📁 当前目录: %cd%
echo 📝 设置UTF-8编码...

set JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8
set MAVEN_OPTS=-Dfile.encoding=UTF-8

echo 🔥 启动热部署监控和Spring Boot应用...
npm run dev-win

pause

@echo off
chcp 65001
echo 🧪 测试日志配置

echo 📁 当前目录: %cd%
echo 📝 检查logs目录...

if not exist "logs" (
    echo 📁 创建logs目录...
    mkdir logs
)

echo 📋 logs目录内容:
dir logs

echo 🚀 启动Spring Boot应用进行日志测试...
echo 💡 请观察控制台输出和logs目录中的文件变化

set JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8 -Dlogging.config=classpath:logback-spring.xml
set MAVEN_OPTS=-Dfile.encoding=UTF-8

mvn spring-boot:run -f jeecg-module-system/jeecg-system-start/pom.xml -Dspring.profiles.active=dev -Dlogging.level.root=INFO

pause

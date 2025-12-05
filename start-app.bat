@echo off
echo ===================== 启动JeecgBoot应用 =====================

echo 1. 清理并编译项目...
cd jeecg-boot
mvn clean compile -DskipTests -q
if !errorlevel! neq 0 (
    echo 编译失败，请检查错误
    pause
    exit /b 1
)

echo.
echo 2. 启动应用...
echo 正在启动JeecgBoot应用...
echo 启动后请访问: http://localhost:8080/jeecg-boot/
echo 按Ctrl+C可以停止应用
echo.

cd jeecg-module-system\jeecg-system-start
mvn spring-boot:run

pause
@echo off
echo ===================== 测试应用启动 =====================

echo 1. 清理并编译项目...
cd jeecg-boot
call mvn clean compile -DskipTests -q
if !errorlevel! neq 0 (
    echo 编译失败
    pause
    exit /b 1
)

echo.
echo 2. 启动应用...
echo 正在启动，请等待...
echo.

cd jeecg-module-system\jeecg-system-start
call mvn spring-boot:run

pause
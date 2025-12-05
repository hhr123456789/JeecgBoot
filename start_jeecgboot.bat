@echo off
echo ===================== JeecgBoot 启动解决方案 =====================

echo 1. 检查端口占用情况...
netstat -an | findstr :8080
if !errorlevel! neq 0 (
    echo ✓ 8080端口未被占用，可以启动应用
) else (
    echo ⚠ 8080端口被占用，正在停止占用进程...
    for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8080') do (
        taskkill /pid %%a /f 2>nul
    )
)

echo.
echo 2. 检查Java和Maven环境...
mvn -version | findstr "Apache Maven"
if !errorlevel! equ 0 (
    echo ✓ Maven环境正常
) else (
    echo ⚠ Maven环境异常
)

echo.
echo 3. 清理并编译项目...
cd jeecg-boot
mvn clean compile -q -Dmaven.test.skip=true
if !errorlevel! equ 0 (
    echo ✓ 编译成功
) else (
    echo ⚠ 编译失败，请检查依赖
    pause
    exit /b 1
)

echo.
echo 4. 启动应用...
echo 正在启动JeecgBoot应用...
echo 启动后请访问: http://localhost:8080/jeecg-boot/jeecg-system-start/
echo 按Ctrl+C可以停止应用
echo.

cd jeecg-module-system\jeecg-system-start
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8080

echo.
echo 应用已停止
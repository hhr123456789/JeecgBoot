@echo off
echo ===================== 检查和停止8080端口占用 =====================

echo 1. 查看当前8080端口占用情况...
netstat -an | findstr :8080

echo.
echo 2. 查找并停止占用8080端口的进程...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8080') do (
    echo 正在停止进程 %%a
    taskkill /pid %%a /f 2>nul
    if !errorlevel! equ 0 (
        echo 成功停止进程 %%a
    ) else (
        echo 无法停止进程 %%a，可能已不存在
    )
)

echo.
echo 3. 查找Java进程...
tasklist | findstr java

echo.
echo 4. 停止所有Java进程（谨慎操作）...
for /f "tokens=1" %%a in ('tasklist ^| findstr java') do (
    echo 正在停止Java进程 %%a
    taskkill /pid %%a /f 2>nul
    if !errorlevel! equ 0 (
        echo 成功停止Java进程 %%a
    ) else (
        echo 无法停止Java进程 %%a
    )
)

echo.
echo 5. 验证8080端口是否已释放...
netstat -an | findstr :8080
if !errorlevel! neq 0 (
    echo 8080端口已成功释放
) else (
    echo 8080端口仍被占用
)

echo.
echo 6. 启动应用...
echo 请手动执行以下命令启动应用：
echo cd jeecg-boot\jeecg-module-system\jeecg-system-start
echo mvn spring-boot:run

pause
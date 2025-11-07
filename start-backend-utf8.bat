@echo off
rem 设置控制台编码为UTF-8
chcp 65001 >nul

rem 设置Java环境变量
set JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8

echo ========================================
echo      JeecgBoot 后端服务启动脚本
echo ========================================
echo.

echo 正在停止可能存在的Java进程...
taskkill /F /IM java.exe /T 2>nul

echo 等待3秒...
timeout /t 3 /nobreak >nul

echo 正在启动JeecgBoot后端服务...
cd /d "E:\workspace\EMSProject_jeecg\JeecgBoot\jeecg-boot\jeecg-module-system\jeecg-system-start"

rem 使用UTF-8编码启动Java应用
java -Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8 -Duser.timezone=Asia/Shanghai -jar target/jeecg-system-start-3.7.2.jar --spring.profiles.active=dev

pause
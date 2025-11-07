@echo off
chcp 65001 >nul
echo 正在停止可能存在的Java进程...
taskkill /F /IM java.exe /T 2>nul

echo 等待3秒...
timeout /t 3 /nobreak >nul

echo 正在启动JeecgBoot后端服务...
cd /d "E:\workspace\EMSProject_jeecg\JeecgBoot\jeecg-boot\jeecg-module-system\jeecg-system-start"
start /B java -Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8 -jar target/jeecg-system-start-3.7.2.jar --spring.profiles.active=dev

echo 等待服务启动...
timeout /t 10 /nobreak >nul

echo 检查服务状态...
netstat -ano | findstr :8080

echo 后端服务已启动！
pause
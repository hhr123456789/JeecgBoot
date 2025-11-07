@echo off
echo ========================================
echo      JeecgBoot 能源管理系统启动脚本
echo ========================================
echo.

echo 1. 启动后端服务...
call start-backend.bat

echo.
echo 2. 启动前端服务...
call start-frontend.bat

echo.
echo 启动完成！
echo 前端访问地址: http://localhost:3100
echo 后端API地址: http://localhost:8080/jeecg-boot
echo.
pause
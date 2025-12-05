@echo off
echo ===================== JeecgBoot 启动问题完整解决方案 =====================

echo 步骤1: 检查MySQL数据库连接
echo 正在测试数据库连接...
mysql -h 127.0.0.1 -P 3306 -u root -pAbc123456@ -e "SELECT 1;" 2>nul
if !errorlevel! equ 0 (
    echo ✓ 数据库连接成功
) else (
    echo ⚠ 数据库连接失败
    echo 请检查MySQL服务是否启动，密码是否正确
    echo 当前配置密码: Abc123456@
    echo.
    echo 手动启动MySQL服务:
    echo net start mysql
    echo.
    echo 如果密码错误，请修改 application-dev.yml 文件
    pause
    exit /b 1
)

echo.
echo 步骤2: 检查数据库是否存在
mysql -h 127.0.0.1 -P 3306 -u root -pAbc123456@ -e "SHOW DATABASES;" 2>nul | findstr EMSProject_jeecg
if !errorlevel! equ 0 (
    echo ✓ EMSProject_jeecg数据库存在
) else (
    echo ⚠ EMSProject_jeecg数据库不存在，正在创建...
    mysql -h 127.0.0.1 -P 3306 -u root -pAbc123456@ -e "CREATE DATABASE IF NOT EXISTS EMSProject_jeecg DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"
    
    echo 正在导入初始化数据...
    mysql -h 127.0.0.1 -P 3306 -u root -pAbc123456@ EMSProject_jeecg < jeecg-boot\db\jeecgboot-mysql-5.7.sql
    if !errorlevel! equ 0 (
        echo ✓ 数据库初始化完成
    ) else (
        echo ⚠ 数据库初始化失败
        pause
        exit /b 1
    )
)

echo.
echo 步骤3: 检查8080端口占用
netstat -an | findstr :8080 >nul
if !errorlevel! neq 0 (
    echo ✓ 8080端口未被占用
) else (
    echo ⚠ 8080端口被占用，正在清理...
    for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8080') do (
        taskkill /pid %%a /f 2>nul
    )
)

echo.
echo 步骤4: 编译项目
cd jeecg-boot
mvn clean install -DskipTests -q
if !errorlevel! equ 0 (
    echo ✓ 项目编译成功
) else (
    echo ⚠ 项目编译失败
    echo 请检查依赖和配置文件
    pause
    exit /b 1
)

echo.
echo 步骤5: 启动应用
echo ================================================
echo 应用启动信息:
echo - 访问地址: http://localhost:8080/jeecg-boot/
echo - 管理后台: http://localhost:8080/jeecg-boot/jeecg-system-start/
echo - API文档: http://localhost:8080/jeecg-boot/doc.html
echo ================================================
echo.
echo 正在启动，请稍候...
echo.

cd jeecg-module-system\jeecg-system-start
mvn spring-boot:run

echo.
echo 应用已停止
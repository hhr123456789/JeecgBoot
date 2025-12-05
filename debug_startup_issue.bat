@echo off
echo ===================== 检查启动问题 =====================

echo 1. 检查MySQL服务状态...
sc query mysql || echo MySQL服务可能未安装或未启动

echo.
echo 2. 测试数据库连接...
mysql -h 127.0.0.1 -P 3306 -u root -pAbc123456@ -e "SELECT 1;" 2>nul && echo 数据库连接成功 || echo 数据库连接失败

echo.
echo 3. 检查数据库是否存在...
mysql -h 127.0.0.1 -P 3306 -u root -pAbc123456@ -e "SHOW DATABASES;" 2>nul | findstr EMSProject_jeecg && echo EMSProject_jeecg数据库存在 || echo EMSProject_jeecg数据库不存在

echo.
echo 4. 检查项目依赖是否完整...
cd jeecg-boot
mvn dependency:tree -q | findstr jeecg-module-energy || echo 能源模块依赖可能有问题

echo.
echo 5. 尝试编译项目...
mvn compile -q -Dmaven.test.skip=true && echo 编译成功 || echo 编译失败

echo.
echo 6. 检查配置文件中的数据库密码是否正确
echo 当前配置的密码: Abc123456@
echo 如果密码不正确，请修改 application-dev.yml 文件

echo.
echo ===================== 解决方案 =====================
echo 如果数据库连接失败，请执行以下操作：
echo 1. 启动MySQL服务
echo 2. 创建数据库: CREATE DATABASE IF NOT EXISTS EMSProject_jeecg DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
echo 3. 导入初始化数据: mysql -u root -pAbc123456@ EMSProject_jeecg ^< jeecg-boot\db\jeecgboot-mysql-5.7.sql
echo 4. 重新编译项目: mvn clean install -DskipTests
echo 5. 启动项目: mvn spring-boot:run

pause
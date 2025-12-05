@echo off
chcp 65001 >nul
echo ========================================
echo 企业分类分区统计定时任务测试脚本
echo ========================================
echo.

:menu
echo 请选择操作:
echo 1. 检查数据库源数据
echo 2. 查看最近的日志
echo 3. 手动触发同步任务（需要后端运行）
echo 4. 查看汇总表数据
echo 5. 清空汇总表数据（慎用）
echo 6. 查看完整诊断报告
echo 0. 退出
echo.
set /p choice=请输入选项 (0-6):

if "%choice%"=="1" goto check_data
if "%choice%"=="2" goto view_logs
if "%choice%"=="3" goto trigger_sync
if "%choice%"=="4" goto view_summary
if "%choice%"=="5" goto clear_summary
if "%choice%"=="6" goto full_diagnosis
if "%choice%"=="0" goto end
echo 无效选项，请重新选择
goto menu

:check_data
echo.
echo ========================================
echo 正在检查数据库源数据...
echo ========================================
echo.
echo 请确保已配置MySQL环境变量，或修改下面的命令指定MySQL路径
echo.
set /p db_user=请输入数据库用户名 (默认: root):
if "%db_user%"=="" set db_user=root
set /p db_name=请输入数据库名称 (默认: jeecgboot):
if "%db_name%"=="" set db_name=jeecgboot

echo.
echo 执行诊断SQL...
mysql -u %db_user% -p %db_name% < db\check_data.sql

echo.
echo 检查完成！
pause
goto menu

:view_logs
echo.
echo ========================================
echo 查看最近的日志
echo ========================================
echo.
set log_file=jeecg-boot\jeecg-module-system\jeecg-system-start\startup.log
if exist "%log_file%" (
    echo 日志文件: %log_file%
    echo.
    echo 最近50行日志:
    echo ----------------------------------------
    powershell -Command "Get-Content '%log_file%' -Tail 50"
    echo ----------------------------------------
    echo.
    echo 搜索定时任务相关日志:
    echo ----------------------------------------
    findstr /C:"企业分类分区统计" "%log_file%" | powershell -Command "$input | Select-Object -Last 20"
    echo ----------------------------------------
) else (
    echo 错误: 日志文件不存在: %log_file%
    echo 请确保后端已启动并运行过
)
echo.
pause
goto menu

:trigger_sync
echo.
echo ========================================
echo 手动触发同步任务
echo ========================================
echo.
echo 注意: 此操作需要后端服务正在运行
echo.
set /p target_date=请输入要同步的日期 (格式: yyyy-MM-dd, 默认: 昨天):

if "%target_date%"=="" (
    REM 获取昨天的日期
    for /f "tokens=1-3 delims=/ " %%a in ('date /t') do (
        set today=%%c-%%a-%%b
    )
    set target_date=%today%
)

echo.
echo 正在触发同步任务...
echo 目标日期: %target_date%
echo API地址: http://localhost:8080/jeecg-boot/energy/classification/syncByDate?targetDate=%target_date%
echo.

curl -X GET "http://localhost:8080/jeecg-boot/energy/classification/syncByDate?targetDate=%target_date%" 2>nul
if errorlevel 1 (
    echo.
    echo 错误: 无法连接到后端服务
    echo 请确保:
    echo 1. 后端服务已启动 (端口8080)
    echo 2. curl 命令可用
    echo.
    echo 您也可以在浏览器中访问:
    echo http://localhost:8080/jeecg-boot/energy/classification/syncByDate?targetDate=%target_date%
) else (
    echo.
    echo 同步任务已触发，请查看日志了解执行结果
)

echo.
pause
goto menu

:view_summary
echo.
echo ========================================
echo 查看汇总表数据
echo ========================================
echo.
set /p db_user=请输入数据库用户名 (默认: root):
if "%db_user%"=="" set db_user=root
set /p db_name=请输入数据库名称 (默认: jeecgboot):
if "%db_name%"=="" set db_name=jeecgboot

echo.
echo 查询汇总表数据...
mysql -u %db_user% -p %db_name% -e "SELECT stat_date, org_code, energy_type, energy_type_name, time_dimension, total_consumption, total_cost, meter_count FROM tb_energy_classification_summary ORDER BY stat_date DESC, org_code, energy_type LIMIT 20;"

echo.
echo 统计信息:
mysql -u %db_user% -p %db_name% -e "SELECT COUNT(*) as total_records, MIN(stat_date) as earliest_date, MAX(stat_date) as latest_date, COUNT(DISTINCT org_code) as unique_org_codes FROM tb_energy_classification_summary;"

echo.
pause
goto menu

:clear_summary
echo.
echo ========================================
echo 清空汇总表数据
echo ========================================
echo.
echo 警告: 此操作将删除 tb_energy_classification_summary 表中的所有数据！
echo.
set /p confirm=确认要清空吗？(输入 YES 确认):

if not "%confirm%"=="YES" (
    echo 操作已取消
    pause
    goto menu
)

set /p db_user=请输入数据库用户名 (默认: root):
if "%db_user%"=="" set db_user=root
set /p db_name=请输入数据库名称 (默认: jeecgboot):
if "%db_name%"=="" set db_name=jeecgboot

echo.
echo 正在清空数据...
mysql -u %db_user% -p %db_name% -e "DELETE FROM tb_energy_classification_summary;"

echo.
echo 数据已清空！
pause
goto menu

:full_diagnosis
echo.
echo ========================================
echo 生成完整诊断报告
echo ========================================
echo.
set /p db_user=请输入数据库用户名 (默认: root):
if "%db_user%"=="" set db_user=root
set /p db_name=请输入数据库名称 (默认: jeecgboot):
if "%db_name%"=="" set db_name=jeecgboot

set report_file=db\diagnosis_report_%date:~0,4%%date:~5,2%%date:~8,2%_%time:~0,2%%time:~3,2%%time:~6,2%.txt
set report_file=%report_file: =0%

echo.
echo 正在生成诊断报告...
echo 报告文件: %report_file%
echo.

(
    echo ========================================
    echo 企业分类分区统计定时任务诊断报告
    echo 生成时间: %date% %time%
    echo ========================================
    echo.
    echo 1. 实时统计表数据量
    echo ----------------------------------------
    mysql -u %db_user% -p %db_name% -e "SELECT COUNT(*) AS total_records, MIN(dt) AS earliest_date, MAX(dt) AS latest_date, COUNT(DISTINCT module_id) AS unique_modules FROM tb_ep_equ_energy_daycount;"
    echo.
    echo 2. 仪表表配置
    echo ----------------------------------------
    mysql -u %db_user% -p %db_name% -e "SELECT COUNT(*) AS total_modules, COUNT(CASE WHEN isaction = 'Y' THEN 1 END) AS active_modules, COUNT(CASE WHEN sys_org_code IS NOT NULL AND sys_org_code != '' THEN 1 END) AS modules_with_org_code, COUNT(CASE WHEN energy_type IS NOT NULL THEN 1 END) AS modules_with_energy_type FROM tb_module;"
    echo.
    echo 3. 仪表按部门和能源类型分布
    echo ----------------------------------------
    mysql -u %db_user% -p %db_name% -e "SELECT sys_org_code, energy_type, COUNT(*) AS module_count, SUM(CASE WHEN isaction = 'Y' THEN 1 ELSE 0 END) AS active_count FROM tb_module WHERE sys_org_code IS NOT NULL AND sys_org_code != '' GROUP BY sys_org_code, energy_type ORDER BY sys_org_code, energy_type;"
    echo.
    echo 4. 汇总表数据量
    echo ----------------------------------------
    mysql -u %db_user% -p %db_name% -e "SELECT COUNT(*) AS total_records, MIN(stat_date) AS earliest_date, MAX(stat_date) AS latest_date, COUNT(DISTINCT org_code) AS unique_org_codes FROM tb_energy_classification_summary;"
    echo.
    echo 5. 未同步的日期
    echo ----------------------------------------
    mysql -u %db_user% -p %db_name% -e "SELECT DISTINCT DATE(d.dt) AS unsynced_date FROM tb_ep_equ_energy_daycount d INNER JOIN tb_module m ON d.module_id = m.module_id WHERE m.isaction = 'Y' AND m.sys_org_code IS NOT NULL AND m.sys_org_code != '' AND DATE(d.dt) NOT IN (SELECT DISTINCT stat_date FROM tb_energy_classification_summary) ORDER BY unsynced_date DESC LIMIT 10;"
    echo.
    echo ========================================
    echo 诊断报告生成完成
    echo ========================================
) > "%report_file%" 2>&1

echo 诊断报告已保存到: %report_file%
echo.
echo 是否查看报告内容？(Y/N)
set /p view_report=
if /i "%view_report%"=="Y" type "%report_file%"

echo.
pause
goto menu

:end
echo.
echo 感谢使用！
echo.
exit /b 0

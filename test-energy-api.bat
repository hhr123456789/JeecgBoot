@echo off
echo 正在启动后端服务...
cd /d "E:\workspace\EMSProject_jeecg\JeecgBoot\jeecg-boot\jeecg-module-system\jeecg-system-start"
start /B java -jar target/jeecg-system-start-3.7.2.jar --spring.profiles.active=dev

echo 等待服务启动...
timeout /t 15 /nobreak >nul

echo 测试企业分类分区统计接口...
powershell -Command "try { Invoke-RestMethod -Uri 'http://localhost:8080/jeecg-boot/energy/classification/getOrgTree' -Method Get | ConvertTo-Json -Depth 10 } catch { $_.Exception.Message }"

echo.
echo 测试能源类型接口...
powershell -Command "try { Invoke-RestMethod -Uri 'http://localhost:8080/jeecg-boot/energy/classification/getEnergyTypes' -Method Get | ConvertTo-Json -Depth 10 } catch { $_.Exception.Message }"

echo.
echo 测试汇总数据接口...
$body = @{
    orgCode = "A01"
    energyType = "all"
    timeDimension = "month"
    startDate = "2024-01-01"
    endDate = "2024-01-31"
    includeChildren = $true
} | ConvertTo-Json

powershell -Command "try { Invoke-RestMethod -Uri 'http://localhost:8080/jeecg-boot/energy/classification/getSummaryData' -Method Post -ContentType 'application/json' -Body '$body' | ConvertTo-Json -Depth 10 } catch { $_.Exception.Message }"

pause
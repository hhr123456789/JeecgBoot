@echo off
echo 测试能源类型接口...
powershell -Command "try { $response = Invoke-RestMethod -Uri 'http://localhost:8080/jeecg-boot/energy/classification/getEnergyTypes' -Method Get; $response | ConvertTo-Json -Depth 10 } catch { Write-Host 'Error:' $_.Exception.Message }"

echo.
echo 测试部门树接口...
powershell -Command "try { $response = Invoke-RestMethod -Uri 'http://localhost:8080/jeecg-boot/energy/classification/getOrgTree' -Method Get; $response | ConvertTo-Json -Depth 10 } catch { Write-Host 'Error:' $_.Exception.Message }"

pause
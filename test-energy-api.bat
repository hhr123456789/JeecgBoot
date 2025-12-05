@echo off
echo 测试企业分类分区统计API接口
echo.

echo 1. 测试获取部门树...
curl -X GET http://localhost:8080/jeecg-boot/energy/classification/getOrgTree
echo.
echo.

echo 2. 测试获取能源类型...
curl -X GET http://localhost:8080/jeecg-boot/energy/classification/getEnergyTypes
echo.
echo.

echo 3. 测试获取日数据汇总...
curl -X POST http://localhost:8080/jeecg-boot/energy/classification/getSummaryData -H "Content-Type: application/json" -d "{\"orgCode\":\"A01\",\"energyType\":\"all\",\"timeDimension\":\"day\",\"startDate\":\"2025-11-07\",\"endDate\":\"2025-11-07\",\"includeChildren\":true}"
echo.
echo.

echo 4. 测试获取日数据趋势...
curl -X POST http://localhost:8080/jeecg-boot/energy/classification/getTrendData -H "Content-Type: application/json" -d "{\"orgCode\":\"A01\",\"energyType\":\"all\",\"timeDimension\":\"day\",\"startDate\":\"2025-11-07\",\"endDate\":\"2025-11-07\",\"includeChildren\":true}"
echo.
echo.

pause
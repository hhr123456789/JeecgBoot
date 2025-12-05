@echo off
echo 正在连接数据库并插入2025年数据...

mysql -h 127.0.0.1 -u root -pAbc123456@ -D EMSProject_jeecg < insert_2025_energy_classification_data.sql

echo 数据插入完成！
echo 正在验证数据...

mysql -h 127.0.0.1 -u root -pAbc123456@ -D EMSProject_jeecg < check_data.sql

pause
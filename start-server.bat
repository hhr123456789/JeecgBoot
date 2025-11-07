@echo off
echo Starting JeecgBoot application...
cd /d "E:\workspace\EMSProject_jeecg\JeecgBoot\jeecg-boot\jeecg-module-system\jeecg-system-start"
java -jar target/jeecg-system-start-3.7.2.jar --spring.profiles.active=dev
pause
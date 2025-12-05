cd jeecg-boot/jeecg-module-system/jeecg-system-start
java -jar target/jeecg-system-start-3.7.2.jar --spring.profiles.active=dev --server.port=8080 > startup.log 2>&1
type startup.log
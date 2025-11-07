@echo off
echo 正在启动JeecgBoot前端服务...
cd /d "E:\workspace\EMSProject_jeecg\JeecgBoot\jeecgboot-vue3"

echo 检查node_modules是否存在...
if not exist node_modules (
    echo 正在安装依赖...
    npm install
)

echo 启动开发服务器...
npm run dev

pause
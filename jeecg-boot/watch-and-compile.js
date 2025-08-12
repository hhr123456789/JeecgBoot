const fs = require('fs');
const path = require('path');
const { exec } = require('child_process');

// 监控的目录
const watchDir = './jeecg-module-energy/src/main/java';
const configDir = './jeecg-module-energy/src/main/resources';

// 防抖延迟（毫秒）
const debounceDelay = 1000;
let compileTimeout;

console.log('🔥 启动文件监控，实现VSCode热部署...');
console.log(`📁 监控目录: ${watchDir}`);
console.log(`⚙️  监控配置: ${configDir}`);

// 编译函数
function compile() {
    console.log('🔨 检测到文件变化，开始编译...');

    exec('mvn compile -f jeecg-module-energy/pom.xml -q', {
        encoding: 'utf8',
        env: { ...process.env, JAVA_TOOL_OPTIONS: '-Dfile.encoding=UTF-8' }
    }, (error, stdout, stderr) => {
        if (error) {
            console.error('❌ 编译失败:', error.message);
            return;
        }
        if (stderr) {
            console.error('⚠️  编译警告:', stderr);
        }
        console.log('✅ 编译完成！DevTools将自动重启应用...');
        if (stdout) {
            console.log(stdout);
        }
    });
}

// 防抖编译
function debouncedCompile() {
    clearTimeout(compileTimeout);
    compileTimeout = setTimeout(compile, debounceDelay);
}

// 监控Java文件
if (fs.existsSync(watchDir)) {
    fs.watch(watchDir, { recursive: true }, (eventType, filename) => {
        if (filename && filename.endsWith('.java')) {
            console.log(`📝 ${eventType}: ${filename}`);
            debouncedCompile();
        }
    });
    console.log(`👀 正在监控Java文件: ${watchDir}`);
} else {
    console.warn(`⚠️  目录不存在: ${watchDir}`);
}

// 监控配置文件
if (fs.existsSync(configDir)) {
    fs.watch(configDir, { recursive: true }, (eventType, filename) => {
        if (filename && (filename.endsWith('.yml') || filename.endsWith('.properties'))) {
            console.log(`⚙️  ${eventType}: ${filename}`);
            debouncedCompile();
        }
    });
    console.log(`👀 正在监控配置文件: ${configDir}`);
} else {
    console.warn(`⚠️  目录不存在: ${configDir}`);
}

console.log('');
console.log('🚀 监控已启动！修改Java文件或配置文件将自动触发编译。');
console.log('💡 使用 Ctrl+C 停止监控');
console.log('');

// 优雅退出
process.on('SIGINT', () => {
    console.log('\n👋 停止文件监控');
    process.exit(0);
});

process.on('SIGTERM', () => {
    console.log('\n👋 停止文件监控');
    process.exit(0);
});

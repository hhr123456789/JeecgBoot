#!/bin/bash

echo "🚀 启动Spring Boot开发环境（支持热部署）"

echo "📁 当前目录: $(pwd)"
echo "📝 设置UTF-8编码..."

export JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8"
export MAVEN_OPTS="-Dfile.encoding=UTF-8"
export LC_ALL=en_US.UTF-8
export LANG=en_US.UTF-8

echo "🔥 启动热部署监控和Spring Boot应用..."
npm run dev

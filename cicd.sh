#!/bin/bash

# 定义颜色
GREEN='\033[0;32m[info] ' # 提示
RED='\033[0;31m[erro] ' # 错误
YELLOW='\033[1;33m[wrin] ' # 警告
NC='\033[0m' # 重置

# 核心脚本
echo -e "${GREEN}更新代码仓库...${NC}"
# git pull && latest_logs=$(git log -5 --oneline)

echo -e "${GREEN}编译后端项目...${NC}"
cd ./work-topic-selection-backend/ && ./mvnw clean package

echo -e "${GREEN}编译前端项目...${NC}"
cd ../work-topic-selection-frontend/ && npm i && npm run build 

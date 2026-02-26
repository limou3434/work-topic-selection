#!/bin/bash
# 定义颜色
GREEN='\033[0;32m[info] ' # 提示
RED='\033[0;31m[erro] ' # 错误
YELLOW='\033[1;33m[wrin] ' # 警告
NC='\033[0m' # 重置

# 核心脚本
echo -e "${GREEN} 编译后端项目 ${NC}"
cd ./work-topic-selection-backend/ && ./mvnw clean package -DskipTests

echo -e "${GREEN} 推送后端镜像 ${NC}"
sudo docker build -t limou3434/work-topic-selection-backend:1.0.0 .
sudo docker push limou3434/work-topic-selection-backend:1.0.0

echo -e "${GREEN} 编译前端项目 ${NC}"
cd ../work-topic-selection-frontend/ && npm i && npm run build 

echo -e "${GREEN} 推送前端镜像 ${NC}"
sudo docker build -t limou3434/work-topic-selection-frontend:1.0.0 .
sudo docker push limou3434/work-topic-selection-frontend:1.0.0

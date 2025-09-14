#!/bin/bash
# 前置条件
# sudo docker pull caddy:2.10.0
# sudo docker pull mysql:8.0.41
# sudo docker pull redis:7.4.2
# sudo docker pull curlimages/curl
# sudo docker pull openjdk:8-jdk-slim
# git fetch origin && git reset --hard origin/main
# sudo apt update && sudo apt install -y openjdk-17-jdk
# curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.3/install.sh | bash
# \. "$HOME/.nvm/nvm.sh"
# nvm install 22
set -e

# 编译后端
(
  echo "后端编译..."
  cd ./work-topic-selection-backend/ 
  ./mvnw clean package > backend.log 2>&1
  echo "后端编译完成 ✅"
) &

# 编译前端
(
  echo "前端编译..."
  cd ./work-topic-selection-frontend/
  npm i && npm run build > frontend.log 2>&1
  echo "前端编译完成 ✅"
) &

# 等待编译
wait

# 部署服务
(
  echo "后端部署..."
  sudo docker compose down work-topic-selection-frontend && sudo docker compose up -d --build work-topic-selection-frontend > /dev/null
  echo "后端部署完成 ✅"
) &

(
  echo "前端部署..."
  sudo docker compose down work-topic-selection-backend && sudo docker compose up -d --build work-topic-selection-backend > /dev/null
  echo "前端部署完成 ✅"
) &

# 等待部署
wait

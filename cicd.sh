#!/bin/bash
# 前置条件
# sudo docker pull caddy:2.10.0
# sudo docker pull mysql:8.0.41
# sudo docker pull redis:7.4.2
# sudo docker pull curlimages/curl
# sudo docker pull openjdk:8-jdk-slim
# sudo apt update && sudo apt install -y openjdk-17-jdk
# curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.3/install.sh | bash
# \. "$HOME/.nvm/nvm.sh"
# nvm install 22
set -e

# 拉取项目
git pull

# 编译项目
echo "后端编译..." && cd ./work-topic-selection-backend/ && ./mvnw clean package && echo "后端编译完成 ✅"
echo "前端编译..." && cd ../work-topic-selection-frontend/ && npm i && npm run build && echo "前端编译完成 ✅"

# 部署项目
echo "后端部署..." && sudo docker compose down work-topic-selection-backend && sudo docker compose up -d --build work-topic-selection-backend >/dev/null && echo "前端部署完成 ✅"
echo "前端部署..." && sudo docker compose down work-topic-selection-frontend && sudo docker compose up -d --build work-topic-selection-frontend >/dev/null && echo "后端部署完成 ✅"

# 重载网管
echo "网关部署..." && sudo docker compose down work-caddy && sudo docker compose up -d work-caddy

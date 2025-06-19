#!/bin/bash
set -e

# 拉取代码
git fetch origin && git reset --hard origin/main

# 编译后端
(
  echo "后端编译..."
  ./mvnw clean package > server.log 2>&1
  echo "后端编译完成 ✅"
) &

# 编译前端
(
  echo "前端编译..."
  cd ./client/work-topic-selection-react || exit 1
  yarn build > client.log 2>&1
  echo "前端编译完成 ✅"
) &

# 等待编译
wait

# 部署服务
(
  echo "后端部署..."
  sudo docker compose up -d --build work-topic-selection | sudo tee server.log > /dev/null
  echo "后端部署完成 ✅"
) &

(
  echo "前端部署..."
  sudo docker compose up -d --build work-topic-selection-client | sudo tee client.log > /dev/null
  echo "前端部署完成 ✅"
) &

# 等待部署
wait

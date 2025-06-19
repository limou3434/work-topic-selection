#!/bin/bash
# 出错终止
set -e

# 拉取代码
git fetch origin && git reset --hard origin/main

# 编译后端
(
  ./mvnw clean package
  echo "后端编译完成 ✅"
) &

# 编译前端
(
  cd ./client/work-topic-selection-react || exit 1
  yarn build
  echo "前端编译完成 ✅"
) &

# 等待完成
wait

# 项目部署
(
  sudo docker compose up -d --build work-topic-selection
  echo "后端部署完成 ✅"
) &

(
  sudo docker compose up -d --build work-topic-selection-client
  echo "前端端部署完成 ✅"
) &

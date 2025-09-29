#!/bin/bash
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

# 发送邮箱
# sudo tee /etc/msmtprc > /dev/null <<'EOF'
# account default
# host smtp.qq.com
# port 587
# auth on
# user 898738804@qq.com
# password <你的授权码>
# from 898738804@qq.com
# tls on
# tls_starttls on
# logfile /var/log/msmtp.log
# EOF
# 检查访问
echo "准备邮件(等待 10 s...)"
sleep 10
frontend_status=$(curl -s http://127.0.0.1:3001)
if [[ "$frontend_status" == *"<html>"* ]]; then
    frontend_message="✅ 前端存活"
else
    frontend_message="❌ 前端失效"
fi

backend_status=$(curl -s http://127.0.0.1:8001)
if [[ "$backend_status" == *"code"* ]]; then
    backend_message="✅ 后端存活"
else
    backend_message="❌ 后端失效"
fi

message="${backend_message}<br>${frontend_message}"

cat <<EOF | msmtp -t
From: 898738804@qq.com
To: 898738804@qq.com
Subject: 系统消息
Content-Type: text/html; charset=UTF-8

<html>
<body style="font-family: Arial, sans-serif; background-color:#f5f5f5; padding:20px;">
  <div style="max-width:600px; margin:0 auto; background:white; border-radius:8px; padding:30px; box-shadow:0 4px 10px rgba(0,0,0,0.1);">
    <h2 style="color:#00785a; text-align:center;">部署情况</h2>
    <p style="font-size:16px; color:#333;">
      您好，感谢您使用 <b>广州南方学院毕业设计选题系统</b> 。
    </p>
    <p style="font-size:16px; color:#333;">以下是您的部署情况: </p>
    <div style="text-align:center; margin:20px 0;">
      <span style="display:inline-block; font-size:28px; font-weight:bold; color:#fff; background:#00785a; padding:10px 20px; border-radius:6px;">
        ${message}
      </span>
    </div>
    <p style="font-size:14px; color:#666;">
      部署情况非常重要，因为本项目使用单体架构，如果失效需要立刻检查。
    </p>
    <hr style="margin:30px 0; border:none; border-top:1px solid #ddd;"/>
    <p style="font-size:12px; color:#999; text-align:center;">
      此邮件由系统自动发送，请不要直接回复。
    </p>
  </div>
</body>
</html>
EOF

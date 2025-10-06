import React, { useEffect, useState } from "react";
import { Button, notification, Input } from "antd";
import {WebSocketMessage} from "@/type/WebSocketMessage";
import {CheckCircleOutlined, CloseCircleOutlined, InfoCircleOutlined} from "@ant-design/icons";

const WebSocketNotification = () => {
  const topicId = 0;

  useEffect(() => {
    const socket = new WebSocket(`ws://127.0.0.1:8000/global/message`);

    socket.onopen = () => console.log("WebSocket 已连接");
    socket.onclose = () => console.log("WebSocket 已关闭");
    socket.onerror = (err) => console.error("WebSocket 错误", err);

    socket.onmessage = (event) => {
      try {
        const msgObj: WebSocketMessage = JSON.parse(event.data);

        let title = "新消息通知";
        let icon = <InfoCircleOutlined style={{ color: "#1890ff" }} />; // 默认信息图标

        switch (msgObj.typeCode) {
          case -1: // ERROR
            title = "错误";
            icon = <CloseCircleOutlined style={{ color: "#ff4d4f" }} />;
            break;
          case 0: // INFO
            title = "消息";
            icon = <InfoCircleOutlined style={{ color: "#1890ff" }} />;
            break;
          case 1: // TOPIC_STATUS_CHANGE
            title = "变更";
            icon = <CheckCircleOutlined style={{ color: "#52c41a" }} />;
            break;
          default:
            title = "未知";
            icon = <InfoCircleOutlined style={{ color: "#d9d9d9" }} />;
        }

        notification.open({
          message: title,
          description: msgObj.message,
          placement: "topRight",
          duration: 0, // 不自动关闭
          icon,
        });
      } catch (err) {
        console.error("WebSocket 消息解析失败:", err, event.data);
      }
    };

    return () => socket.close();
  }, [topicId]);

  return null;
};

const WebSocketSender = () => {
  const topicId = 0;
  const [ws, setWs] = useState<WebSocket | null>(null);
  const [msg, setMsg] = useState("");

  useEffect(() => {
    const socket = new WebSocket(`ws://127.0.0.1:8000/global/message`);
    socket.onopen = () => console.log("WebSocket 已连接");
    socket.onclose = () => console.log("WebSocket 已关闭");
    socket.onerror = (err) => console.error("WebSocket 错误", err);
    setWs(socket);
    return () => socket.close();
  }, [topicId]);

  const sendMessage = () => {
    if (ws && ws.readyState === WebSocket.OPEN) {
      // 构造消息对象
      const payload: WebSocketMessage = {
        typeCode: 0, // 可以根据需要设置类型，例如 0=INFO
        message: msg || `Hello at ${new Date().toLocaleTimeString()}`,
        extend: {}, // 可选扩展字段
      };

      // 发送 JSON 字符串
      ws.send(JSON.stringify(payload));

      // 清空输入框
      setMsg("");
    }
  };

  return (
    <div
      style={{
        padding: 10,
        display: "flex",
        flexDirection: "row",
        alignItems: "center",
        gap: 10,
        maxWidth: 700,       // 最大宽度
        width: "80%",         // 屏幕宽度的 80%
        margin: "0 auto",
        flexWrap: "wrap",     // 小屏幕自动换行
      }}
    >
      <Input
        placeholder="向在线用户发送全局消息..."
        value={msg}
        onChange={(e) => setMsg(e.target.value)}
        style={{ flex: 1, minWidth: 150 }} // 输入框占剩余空间
      />
      <Button type="primary" onClick={sendMessage} style={{ flexShrink: 0 }}>
        发送消息
      </Button>
    </div>
  );
};

export {WebSocketNotification, WebSocketSender}

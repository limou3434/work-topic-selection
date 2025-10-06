import { useEffect, useState } from "react";
import { Button, notification, Input } from "antd";
import { CheckCircleOutlined, CloseCircleOutlined, InfoCircleOutlined } from "@ant-design/icons";

/** WebSocket 消息类型（对应后端 Java WebSocketMessage） */
interface WebSocketMessage {
  typeCode: number;
  message: string;
  extend?: Record<string, any>;
}

/** 全局 WebSocket 单例 */
let globalSocket: WebSocket | null = null;

/** 获取（或初始化）WebSocket */
function getSocket(): WebSocket {
  if (!globalSocket || globalSocket.readyState === WebSocket.CLOSED) {
    globalSocket = new WebSocket("ws://127.0.0.1:8000/global/message?id=1");
    globalSocket.onopen = () => console.log("✅ WebSocket 已连接");
    globalSocket.onclose = () => console.log("❎ WebSocket 已关闭");
    globalSocket.onerror = (err) => console.error("⚠️ WebSocket 有错误", err);
  }
  return globalSocket;
}

/** 通知组件：监听消息并显示 Antd 通知 */
const WebSocketNotification = () => {
  useEffect(() => {
    const socket = getSocket();

    socket.onmessage = (event) => {
      try {
        const msgObj: WebSocketMessage = JSON.parse(event.data);
        let title = "新消息通知";
        let icon = <InfoCircleOutlined style={{ color: "#1890ff" }} />;

        switch (msgObj.typeCode) {
          case -1:
            title = "错误";
            icon = <CloseCircleOutlined style={{ color: "#ff4d4f" }} />;
            break;
          case 0:
            title = "消息";
            icon = <InfoCircleOutlined style={{ color: "#1890ff" }} />;
            break;
          case 1:
            title = "变更";
            icon = <CheckCircleOutlined style={{ color: "#52c41a" }} />;
            break;
        }

        notification.open({
          message: title,
          description: msgObj.message,
          placement: "topRight",
          duration: 0,
          icon,
        });
      } catch (err) {
        console.error("消息解析失败:", err, event.data);
      }
    };
  }, []);

  return null;
};

/** 发送组件：用于手动向服务端发消息 */
const WebSocketSender = () => {
  const [msg, setMsg] = useState("");

  const sendMessage = () => {
    const socket = getSocket();
    if (socket.readyState === WebSocket.OPEN) {
      const payload: WebSocketMessage = {
        typeCode: 0,
        message: msg || `Hello at ${new Date().toLocaleTimeString()}`,
        extend: {},
      };
      socket.send(JSON.stringify(payload));
      setMsg("");
    } else {
      console.warn("WebSocket 未连接，无法发送消息");
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
        maxWidth: 700,
        width: "80%",
        margin: "0 auto",
        flexWrap: "wrap",
      }}
    >
      <Input
        placeholder="向所有在线用户发送全局消息..."
        value={msg}
        onChange={(e) => setMsg(e.target.value)}
        style={{ flex: 1, minWidth: 150 }}
      />
      <Button type="primary" onClick={sendMessage} style={{ flexShrink: 0 }}>
        发送消息
      </Button>
    </div>
  );
};

export { WebSocketNotification, WebSocketSender };

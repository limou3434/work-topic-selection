import { Image, notification } from 'antd';
import React, { useEffect } from 'react';

const QRCodeNotification: React.FC = () => {
  useEffect(() => {
    // 检查是否已经弹过
    if (sessionStorage.getItem('qrCodeNotificationShown')) return;

    notification.info({
      message: '加入群聊',
      description: (
        <>
          扫描下方二维码加入选题系统交流群，获取最新通知和帮助：
          <div style={{ textAlign: 'center', marginTop: 10 }}>
            <Image
              src="/orcode.png"
              style={{ width: '80%', maxWidth: 200, borderRadius: 8 }}
              preview={false}
            />
          </div>
          <div style={{ textAlign: 'center', marginTop: 10, fontSize: '12px', color: '#888' }}>
            二维码失效请联系管理员
          </div>
        </>
      ),
      placement: 'bottomRight',
      duration: 0, // 不自动关闭
    });

    // 设置标记，防止重复弹出
    sessionStorage.setItem('qrCodeNotificationShown', 'true');
  }, []);

  return null;
};

export default QRCodeNotification;

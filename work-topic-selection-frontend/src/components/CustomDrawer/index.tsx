import React from 'react';
import { Drawer, Typography } from 'antd';

const { Title, Paragraph } = Typography;

interface CustomDrawerProps {
  visible: boolean;
  onClose: () => void;
  title?: string;
}

const CustomDrawer: React.FC<CustomDrawerProps> = ({
  visible,
  onClose,
  title = '自定义抽屉'
}) => {
  return (
    <Drawer
      title={title}
      placement="right"
      onClose={onClose}
      open={visible}
      width={600}
    >
      <div style={{ padding: '16px 0' }}>
        <Title level={4}>欢迎使用自定义抽屉</Title>
        <Paragraph>
          这是一个独立封装的抽屉组件，您可以在这里进行后续开发。
        </Paragraph>
        <Paragraph>
          抽屉已经准备就绪，可以添加您需要的任何内容和功能。
        </Paragraph>
        <Paragraph type="secondary">
          这个组件是可复用的，您可以通过 props 传递不同的配置来自定义它的行为。
        </Paragraph>
      </div>
    </Drawer>
  );
};

export default CustomDrawer;
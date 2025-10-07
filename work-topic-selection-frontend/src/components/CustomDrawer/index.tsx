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
        <Title level={4}>欢迎使用智能寻题拓展功能</Title>
        <Paragraph>
          本组件暂未开放...
        </Paragraph>
      </div>
    </Drawer>
  );
};

export default CustomDrawer;

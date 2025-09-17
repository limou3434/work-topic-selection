import { useState, useRef } from 'react';
import { Button, Input, message, Popconfirm, Space } from 'antd';
import { deleteUserUsingPost } from '@/services/work-topic-selection/userController';

// @ts-ignore
const AdjustLimitButton = ({ record, action }) => {
  const [editing, setEditing] = useState(false); // 是否显示输入框
  const [value, setValue] = useState('');        // 输入框当前值
  const [loading, setLoading] = useState(false); // 确认按钮 loading
  const currentValueRef = useRef('');            // 保存原始值，方便对比

  // 点击确认时提交
  const handleConfirm = async () => {
    setLoading(true);
    try {
      if (value === currentValueRef.current) {
        message.info('值未修改');
      } else {
        // TODO: 调用提交接口
        // await submitNewLimit({ userAccount: record.userAccount, limit: value });
        message.success(`已提交新值: ${value}`);
        action?.reload?.();
      }
      setEditing(false);
    } catch {
      message.error('提交失败');
    } finally {
      setLoading(false);
    }
  };

  // 点击“调整选题数量上限”，读取当前值并打开输入框
  const handleModifyClick = async () => {
    try {
      // TODO: 从接口获取当前值，例如：
      // const currentValue = await fetchCurrentLimit(record.userAccount);
      const currentValue = '5'; // 这里假设为 5
      currentValueRef.current = currentValue;
      setValue(currentValue);
      setEditing(true);
    } catch (err) {
      message.error('获取当前值失败');
    }
  };

  if (editing) {
    return (
      <Space>
        <Input
          value={value}
          onChange={(e) => setValue(e.target.value)}
          style={{ width: 100 }}
          placeholder="输入新值"
        />
        <Button type="primary" size="small" loading={loading} onClick={handleConfirm}>
          确认
        </Button>
        <Button size="small" onClick={() => setEditing(false)}>
          取消
        </Button>
      </Space>
    );
  }

  return (
    <Space>
      <a style={{ color: '#454be3' }} onClick={handleModifyClick}>
        调整选题数量上限
      </a>
      <Popconfirm
        title="确定要删除该用户吗？"
        onConfirm={async () => {
          const res = await deleteUserUsingPost({ userAccount: record.userAccount });
          if (res.code === 0) {
            message.success(res.message);
            action?.reload?.();
          } else {
            message.error(res.message);
          }
        }}
        okText="确定"
        cancelText="取消"
      >
        <a style={{ color: '#ff4d4f' }}>删除</a>
      </Popconfirm>
    </Space>
  );
};

export { AdjustLimitButton };

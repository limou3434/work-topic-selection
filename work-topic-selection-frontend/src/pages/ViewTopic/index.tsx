import React, { useEffect, useState } from 'react';
import { Card, Button, Descriptions, message, Spin, Empty, Flex, Modal, Typography, Alert } from 'antd';
import { LockOutlined, UnlockOutlined } from '@ant-design/icons';
import {
  getSelectTopicUsingPost,
  withdrawUsingPost,
  getTopicLockUsingGet,
} from '@/services/work-topic-selection/userController';

const { Title } = Typography;

// 滚动公告组件
const ScrollingNotice = () => {
  return (
    <div
      style={{
        backgroundColor: '#fff7e6',
        border: '1px solid #ffd666',
        borderRadius: '4px',
        height: '40px',
        overflow: 'hidden',
        display: 'flex',
        alignItems: 'center',
        maxWidth: 800,
        width: 'calc(100% - 32px)', // 减去左右margin
        margin: '16px auto',
      }}
    >
      <div
        style={{
          whiteSpace: 'nowrap',
          animation: 'scroll-left 20s linear infinite',
          fontSize: '14px',
          color: '#d46b08',
          fontWeight: '500',
          paddingLeft: '100%',
        }}
      >
        📧 请学生们务必绑定帐号邮箱，否则教师一旦退选您的题目将无法及时获取通知！
      </div>
      <style>
        {`
          @keyframes scroll-left {
            0% {
              transform: translateX(0);
            }
            100% {
              transform: translateX(-100%);
            }
          }

          /* 移动端响应式处理 */
          @media (max-width: 768px) {
            div[style*="height: 40px"] {
              height: auto;
              min-height: 40px;
            }

            div[style*="whiteSpace: 'nowrap'"] {
              whiteSpace: normal;
              animation: none;
              padding: 8px 12px;
              paddingLeft: 0;
            }
          }

          /* 小屏幕设备进一步优化 */
          @media (max-width: 480px) {
            div[style*="height: 40px"] {
              height: auto;
              min-height: 50px;
            }

            div[style*="fontSize: '14px'"] {
              fontSize: '12px';
            }
          }
        `}
      </style>
    </div>
  );
};

export default () => {
  const [loading, setLoading] = useState(true);
  const [topic, setTopic] = useState<API.Topic | null>(null);
  const [topicLocked, setTopicLocked] = useState<boolean>(false);

  const fetchTopic = async () => {
    setLoading(true);
    try {
      const res = await getSelectTopicUsingPost();
      if (res.code === 0 && res.data && res.data.length > 0) {
        setTopic(res.data[0]); // 只显示第一个选题
      } else {
        setTopic(null);
      }
    } catch (e: any) {
      message.error(e.message || '获取选题信息失败');
    } finally {
      setLoading(false);
    }
  };

  const fetchTopicLockStatus = async () => {
    try {
      const res = await getTopicLockUsingGet();
      if (res.code === 0) {
        setTopicLocked(res.data || false);
      }
    } catch (e: any) {
      console.error('获取选题锁定状态失败:', e);
    }
  };

  const handleWithdraw = async () => {
    if (!topic?.id) return;

    Modal.confirm({
      title: '谨慎操作',
      content: '您确定要退选当前题目吗？',
      okText: '确认',
      cancelText: '取消',
      onOk: async () => {
        const res = await withdrawUsingPost({ id: topic.id });
        if (res.code === 0) {
          message.success('退选成功');
          fetchTopic(); // 刷新数据
        } else {
          message.error(res.message || '退选失败');
        }
      },
    });
  };

  useEffect(() => {
    fetchTopic();
    fetchTopicLockStatus();
  }, []);

  return (
    <Spin spinning={loading}>
      <Title level={2} style={{
        textAlign: 'center',
        margin: '16px 0',
        padding: '0 16px',
        wordBreak: 'break-word'
      }}>我的选题</Title>
      <ScrollingNotice />
      <Flex justify="center" align="center" style={{
        minHeight: 300,
        width: '100%',
        padding: '0 16px'
      }}>
        {topic ? (
          <Card
            title={topic.topic}
            style={{
              width: '100%',
              maxWidth: 800,
            }}
            extra={
              <Button
                danger={!topicLocked}
                disabled={topicLocked}
                onClick={handleWithdraw}
                icon={topicLocked ? <LockOutlined /> : <UnlockOutlined />}
              >
                {topicLocked ? '锁定' : '退选'}
              </Button>
            }
          >
            <Descriptions
              column={1}
              bordered
              size="small"
              style={{
                maxWidth: '100%',
                wordBreak: 'break-word'
              }}
            >
              <Descriptions.Item label="题目类型">{topic.type}</Descriptions.Item>
              <Descriptions.Item label="题目描述">{topic.description}</Descriptions.Item>
              <Descriptions.Item label="学生要求">{topic.requirement}</Descriptions.Item>
              <Descriptions.Item label="指导老师">{topic.teacherName}</Descriptions.Item>
              <Descriptions.Item label="所属学院">{topic.deptName}</Descriptions.Item>
            </Descriptions>
          </Card>
        ) : (
          <Empty description="暂无选题数据" />
        )}
      </Flex>
    </Spin>
  );
};

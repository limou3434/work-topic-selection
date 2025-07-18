import React, { useEffect, useState } from 'react';
import { Card, Button, Descriptions, message, Spin, Empty, Flex } from 'antd';
import {
  getSelectTopicUsingPost,
  withdrawUsingPost,
} from '@/services/work-topic-selection/userController';

export default () => {
  const [loading, setLoading] = useState(true);
  const [topic, setTopic] = useState<API.Topic | null>(null);

  const fetchTopic = async () => {
    setLoading(true);
    try {
      const res = await getSelectTopicUsingPost();
      if (res.code === 0 && res.data && res.data.length > 0) {
        setTopic(res.data[0]); // 只显示第一个选题
      } else {
        setTopic(null);
      }
    } catch (e) {
      message.error(res.message);
    } finally {
      setLoading(false);
    }
  };

  const handleWithdraw = async () => {
    if (!topic?.id) return;
    const res = await withdrawUsingPost({ id: topic.id });
    if (res.code === 0) {
      message.success(res.message);
      fetchTopic(); // 刷新数据
    } else {
      message.error(res.message);
    }
  };

  useEffect(() => {
    fetchTopic();
  }, []);

  return (
    <Spin spinning={loading}>
      <Flex justify="center" align="center" style={{ minHeight: 300 }}>
        {topic ? (
          <Card
            title={topic.topic}
            style={{ width: 800 }}
            extra={<Button danger onClick={handleWithdraw}>退选</Button>}
          >
            <Descriptions column={1} bordered size="small">
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

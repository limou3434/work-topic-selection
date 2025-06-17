import { ProColumns, ProTable, ActionType } from '@ant-design/pro-components';
import { message } from 'antd';
import React, { useEffect, useRef } from 'react';
import {
  getTopicListUsingPost,
  preSelectTopicByIdUsingPost,
} from '@/services/work-topic-selection/userController';
import './index.css'; // 引入样式文件

export type TableListItem = {
  id: number;
  topic: string;
  surplusQuantity: number;
  selectAmount: number;
  type: string;
  description: string;
  requirement: string;
};

const columns: ProColumns<TableListItem>[] = [
  {
    title: '序号',
    dataIndex: 'id',
    valueType: 'indexBorder',
    width: 48,
  },
  {
    title: '相关操作',
    valueType: 'option',
    width: 80,
    render: (_, record, __, action) => {
      const disabled = record.surplusQuantity === 0;
      return [
        <a
          key="select"
          onClick={async () => {
            if (disabled) return;
            const res = await preSelectTopicByIdUsingPost({ id: record.id, status: 0 });
            if (res.code === 0) {
              message.success(res.message);
            } else {
              message.error(res.message);
            }
            action?.reload();
          }}
          style={{
            color: disabled ? 'gray' : undefined,
            pointerEvents: disabled ? 'none' : 'auto',
            cursor: disabled ? 'not-allowed' : 'pointer',
          }}
        >
          预选题目
        </a>,
      ];
    },
  },
  {
    title: '剩余数量',
    dataIndex: 'surplusQuantity',
    search: false,
    width: 80,
  },
  {
    title: '预选数量',
    dataIndex: 'selectAmount',
    search: false,
    width: 80,
  },
  {
    title: '题目标题',
    dataIndex: 'topic',
  },
  {
    title: '题目类型',
    dataIndex: 'type',
    width: 100,
  },
  {
    title: '题目描述',
    dataIndex: 'description',
    valueType: 'textarea',
  },
  {
    title: '要求学生',
    dataIndex: 'requirement',
    valueType: 'textarea',
  },
];

const TopicTable: React.FC<{ teacherName: string }> = ({ teacherName }) => {
  const actionRef = useRef<ActionType>();

  useEffect(() => {
    actionRef.current?.reload();
  }, [teacherName]);

  return (
    <ProTable<TableListItem>
      actionRef={actionRef}
      columns={columns}
      request={async (params = {}) => {
        try {
          const res = await getTopicListUsingPost({
            ...params,
            status: '1',
            teacherName,
          });
          return {
            data: res.data?.records || [],
            total: res.data?.total || 0,
            success: true,
          };
        } catch {
          return {
            data: [],
            total: 0,
            success: false,
          };
        }
      }}
      scroll={{ x: 1300 }}
      search={false}
      pagination={{ pageSize: 10 }}
      rowKey="id"
      rowClassName={(record) => (record.surplusQuantity === 0 ? 'row-disabled' : '')}
    />
  );
};

export default TopicTable;

import { ProColumns } from '@ant-design/pro-components';
import { ProTable } from '@ant-design/pro-components';
import { message } from 'antd';
import React from "react";
import {
  getTopicListUsingPost, preSelectTopicByIdUsingPost,
} from "@/services/work-topic-selection/userController";
import { useParams } from "react-router-dom";

export type TableListItem = {
  id: number;
  key: number;
  name: string;
  progress: number;
  containers: number;
  callNumber: number;
  creator: string;
  status: string;
  createdAt: number;
  memo: string;
};

const columns: ProColumns<TableListItem>[] = [
  {
    dataIndex: 'id',
    valueType: 'indexBorder',
    width: 48,
  },
  {
    title: '操作',
    valueType: 'option',
    key: 'option',
    width: 70,
    render: (text, record, _, action) => [
      <a
        key="select"
        onClick={async () => {
          const res = await preSelectTopicByIdUsingPost({ id: record.id, status: 1 });
          if (res.code === 0) {
            message.success(res.message);
          } else {
            message.error(res.message);
          }
          action?.reload();
        }}
      >
        预选题
      </a>,
    ],
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
    title: '题目',
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

const TopicTable: React.FC = () => {
  const { teacherName } = useParams<{ teacherName: string }>();

  return (
    <ProTable<TableListItem>
      columns={columns}
      //@ts-ignore
      request={async (params = {}, sort, filter) => {
        console.log(sort, filter, params);
        try {
          const paramsWithFormName = { ...params, status: '1', teacherName: teacherName };
          const response = await getTopicListUsingPost(paramsWithFormName);
          return {
            //@ts-ignore
            data: response.data.records,
            //@ts-ignore
            total: response.data.total, // 确保包含 total 字段
          };
        } catch (error) {
          console.error('Error fetching data:', error);
          return {
            data: [],
            total: 0,
          };
        }
      }}
      scroll={{ x: 1300 }}
      options={false}
      search={{
        labelWidth: 'auto',
      }}
      pagination={{
        pageSize: 30,
      }}
      rowKey="id" // 确保使用唯一标识符
      headerTitle="题目"
    />
  );
};

export default TopicTable;

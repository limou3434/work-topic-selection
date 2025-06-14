import {ProColumns} from '@ant-design/pro-components';
import { ProTable } from '@ant-design/pro-components';
import React from "react";
import {
  getSelectTopicUsingPost, withdrawUsingPost
} from "@/services/work-topic-selection/userController";
import {message} from "antd";

export type TableListItem = {
  id?:number;
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
    title: '题目',
    dataIndex: 'topic',
  },
  {
    title: '题目类型',
    dataIndex: 'type',

  },
  {
    title: '题目描述',
    dataIndex: 'description',
    valueType: 'textarea',
  },
  {
    title: '对学生要求',
    dataIndex: 'requirement',
    valueType: 'textarea',
  },
  {
    title: '指导老师',
    dataIndex: 'teacherName',
    valueType: 'select',
  },
  {
    title: '操作',
    valueType: 'option',
    key: 'option',
    render: (text, record, _,action) => [
      <a
        key="select"
        onClick={async () => {

          const res = await withdrawUsingPost({id: record.id})
          if (res.code === 0) {
            message.success(res.message)
          } else {
            message.error(res.message)
          }
          action?.reload();
        }}
      >
        退选
      </a>,
    ],
  },
];

export default () => {
  return (
    <ProTable<TableListItem>
      columns={columns}
      //@ts-ignore
      request={async (params = {}, sort, filter) => {
        console.log(sort, filter, params);
        try {
          const response = await getSelectTopicUsingPost();
          return {
            data: response.data || []
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
      search={false}
      pagination={{
        pageSize: 30,
      }}
      rowKey="key"
      headerTitle="选择题目"
    />
  );
};

import {ProColumns} from '@ant-design/pro-components';
import { ProTable } from '@ant-design/pro-components';
import {message} from 'antd';
import React from "react";
import {
  getPreTopicUsingPost,
  preSelectTopicByIdUsingPost,
  selectTopicByIdUsingPost
} from "@/services/bsxt/userController";

export type TableListItem = {
  id:number;
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
    search:false
  },
  {
    title: '指导老师',
    dataIndex: 'teacherName',
    valueType: 'select',
    search:false
  },
  {
    title: '预选人数',
    dataIndex: 'selectAmount',
    search:false
  },
  {
    title: '剩余数量',
    dataIndex: 'surplusQuantity',
    search:false
  },
  {
    title: '开启时间',
    dataIndex: 'startTime',
    valueType: 'dateTime',
    search:false
  },
  {
    title: '结束时间',
    dataIndex: 'endTime',
    valueType: 'dateTime',
    search:false
  },
  {
    title: '操作',
    valueType: 'option',
    key: 'option',
    render: (text, record, _,action) => [
      <a
        key="select"
        onClick={async () => {
          const res = await selectTopicByIdUsingPost({id: record.id,status:1})
          if (res.code === 0) {
            message.success(res.message)

          } else {
            message.error(res.message)
          }
          action?.reload();
        }}
      >
        选题
      </a>,
      <a
        key="delete"
        onClick={async () => {
          const res = await preSelectTopicByIdUsingPost({id: record.id,status:-1})
          if (res.code === 0) {
            message.success(res.message)
          } else {
            message.error(res.message)
          }
          action?.reload();
        }}
      >
        退预选题
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
          const response = await getPreTopicUsingPost();
          return {
            // @ts-ignore
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

// @ts-ignore
import { listUserByPageUsingPost, selectStudentUsingPost } from '@/services/work-topic-selection/userController';
import { ActionType, ProColumns } from '@ant-design/pro-components';
import { ProTable } from '@ant-design/pro-components';
import React, { useRef } from 'react';
import { message } from 'antd';
import { useParams } from "react-router-dom";

type GithubIssueItem = {
  userAccount: string;
  userName: string;
  dept: string;
  project: string;
};

export default () => {
  const { topic } = useParams();
  const actionRef = useRef<ActionType>();

  const columns: ProColumns<GithubIssueItem>[] = [
    {
      title: '序号',
      dataIndex: 'id',
      valueType: 'indexBorder',
      width: 48,
    },
    {
      title: '学号',
      dataIndex: 'userAccount',
    },
    {
      title: '名字',
      dataIndex: 'userName',
    },
    {
      title: '系部',
      dataIndex: 'dept',
    },
    {
      title: '专业',
      dataIndex: 'project',
    },
    {
      title: '操作',
      valueType: 'option',
      key: 'option',
      render: (text, record, _, action) => [
        <a
          key="selectStudent"
          onClick={async () => {
            const res = await selectStudentUsingPost({ userAccount: record.userAccount, topic: topic });
            if (res.code === 0) {
              message.success(res.message);
            } else {
              message.error(res.message);
            }
            action?.reload();
          }}
        >
          选择
        </a>,
      ],
    },
  ];

  return (
    <ProTable<GithubIssueItem>
      columns={columns}
      actionRef={actionRef}
      cardBordered
      // @ts-ignore
      request={async (params = {}, sort, filter) => {
        console.log(sort, filter, params);
        try {
          const paramsWithFormName = { ...params, userRole: 0, };
          const response = await listUserByPageUsingPost(paramsWithFormName);
          const records = response?.data?.records || [];
          const total = response?.data?.total || 0;
          return {
            data: records,
            total: total,
            success: true,
          };
        } catch (error) {
          console.error('Error fetching data:', error);
          return {
            data: [],
            total: 0,
            success: false,
          };
        }
      }}
      editable={{
        type: 'multiple',
      }}
      columnsState={{
        persistenceKey: 'pro-table-singe-demos',
        persistenceType: 'localStorage',
      }}
      rowKey="id"
      search={{
        labelWidth: 'auto',
      }}
      form={{
        syncToUrl: (values, type) => {
          if (type === 'get') {
            return {
              ...values,
            };
          }
          return values;
        },
      }}
      pagination={{
        pageSize: 5,
        showQuickJumper: true,
      }}
      dateFormatter="string"
      headerTitle="学生"
      toolBarRender={() => []}
    />
  );
};

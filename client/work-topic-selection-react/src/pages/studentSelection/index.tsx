// @ts-ignore
import {
  getTeacherUsingPost,
} from '@/services/work-topic-selection/userController';
import { ActionType, ProColumns, ProTable } from '@ant-design/pro-components';
import React, { useRef } from 'react';
import { useNavigate } from "react-router-dom";

type GithubIssueItem = {
  topicAmount: number;
  teacherName: string;
  selectAmount: number;
};

export default () => {
  const actionRef = useRef<ActionType>();
  const navigate = useNavigate();

  const columns: ProColumns<GithubIssueItem>[] = [
    {
      dataIndex: 'id',
      valueType: 'indexBorder',
      width: 48,
    },
    {
      title: '姓名',
      dataIndex: 'teacherName',
    },
    {
      title: '题目数量',
      dataIndex: 'topicAmount',
      search: false,
    },
    {
      title: '预选人数',
      dataIndex: 'selectAmount',
      search: false,
    },
    {
      title: '操作',
      valueType: 'option',
      key: 'option',
      render: (_, record) => [
        <a
          key="editable"
          onClick={() => {
            navigate(`/topic/student/select/${record.teacherName}`);
          }}
        >
          查看题目
        </a>,
      ],
    },
  ];

  return (
    <ProTable<GithubIssueItem>
      columns={columns}
      actionRef={actionRef}
      cardBordered
      /* eslint-disable-next-line @typescript-eslint/no-unused-vars */
      request={async (params = {}, sort, filter) => {
        try {
          const response = await getTeacherUsingPost({
            ...params,
            pageNumber: params.current,
            pageSize: params.pageSize,
          });
          return {
            data: response.data?.records || [],
            total: response.data?.total || 0,
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
          return type === 'get' ? { ...values } : values;
        },
      }}
      pagination={{
        pageSize: 10, // 默认10条
        showSizeChanger: true,
        pageSizeOptions: ['10', '20', '50'],
        showQuickJumper: true,
      }}
      dateFormatter="string"
      headerTitle="题目"
      toolBarRender={false}
    />
  );
};

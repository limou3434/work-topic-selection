// @ts-ignore
import {
  getTeacherUsingPost,
} from '@/services/work-topic-selection/userController';
import {ActionType, ProColumns} from '@ant-design/pro-components';
import { ProTable } from '@ant-design/pro-components';
import React, { useRef } from 'react';
import {useNavigate} from "react-router-dom";

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
      search:false
    },
    {
      title: '预选人数',
      dataIndex: 'selectAmount',
      search: false
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
      // @ts-ignore
      request={async (params = {}, sort, filter) => {
        console.log(sort, filter, params);
        try {
          const paramsWithFormName = { ...params};
          const response = await getTeacherUsingPost(paramsWithFormName);
          return {
            // @ts-ignore
            data: response.data.records
          };
        } catch (error) {
          console.error('Error fetching data:', error);
          return {
            data: [],
            total: 0,
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
      }}
      dateFormatter="string"
      headerTitle="题目"
      toolBarRender={() => [
      ]}
    />
  );
};

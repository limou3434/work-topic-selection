import {
  addUserUsingPost,
  deleteUserUsingPost,
  listUserByPageUsingPost,
  resetPasswordUsingPost,
} from '@/services/work-topic-selection/userController';
import { PlusOutlined } from '@ant-design/icons';
import { ActionType, ProColumns, ProFormText, ProTable } from '@ant-design/pro-components';
import { ModalForm } from '@ant-design/pro-form';
import { Button, message } from 'antd';
import React, { useRef } from 'react';

type GithubIssueItem = {
  userAccount: string;
  userName: string;
  dept: string;
};

export default () => {
  const actionRef = useRef<ActionType>();

  const columns: ProColumns<GithubIssueItem>[] = [
    {
      dataIndex: 'id',
      valueType: 'indexBorder',
      width: 48,
    },
    {
      title: '工号',
      dataIndex: 'userAccount',
    },
    {
      title: '名字',
      dataIndex: 'userName',
    },
    {
      title: '操作',
      valueType: 'option',
      key: 'option',
      render: (text, record, _, action) => [
        <a
          style={{ color: '#ff4d4f' }} // Ant Design 默认危险色
          key="editable"
          onClick={async () => {
            const res = await deleteUserUsingPost({ userAccount: record.userAccount });
            if (res.code === 0) {
              message.success(res.message);
              action?.reload();
            } else {
              message.error(res.message);
            }
          }}
        >
          删除
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
          const paramsWithUserRole = { ...params, userRole: 3 };
          const response = await listUserByPageUsingPost(paramsWithUserRole);
          return {
            // @ts-ignore
            data: response.data.records,
            // @ts-ignore
            total: response.data.total,
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
        showSizeChanger: true, // 允许用户选择每页显示条目数
      }}
      dateFormatter="string"
      headerTitle="系统账号管理"
      toolBarRender={() => [
        <React.Fragment key="toolbar">
          <ModalForm<{
            userAccount: string;
            userName: string;
          }>
            title="添加系统账号"
            trigger={
              <Button type="primary">
                <PlusOutlined /> 添加系统账号
              </Button>
            }
            autoFocusFirstInput
            modalProps={{
              destroyOnClose: true,
              onCancel: () => console.log('run'),
            }}
            submitTimeout={2000}
            onFinish={async (values) => {
              const res = await addUserUsingPost({...values, userRole: 3});
              if (res.code === 0) {
                message.success(res.message);
                actionRef.current?.reload();
                return true;
              } else {
                message.error(res.message);
                return false;
              }
            }}
          >
            <ProFormText width="md" name="userAccount" label="工号" required={true} />
            <ProFormText width="md" name="userName" label="姓名" required={true} />
          </ModalForm>
        </React.Fragment>,
        // eslint-disable-next-line react/jsx-key
        <ModalForm<{
          userAccount: string;
          userName: string;
        }>
          title="重置账号密码"
          trigger={
            <Button type="primary" ghost>
              <PlusOutlined />
              重置账号密码
            </Button>
          }
          autoFocusFirstInput
          modalProps={{
            destroyOnClose: true,
            onCancel: () => console.log('run'),
          }}
          submitTimeout={2000}
          onFinish={async (values) => {
            const res = await resetPasswordUsingPost(values);
            if (res.code === 0) {
              message.success(res.message);
              actionRef.current?.reload();
              return true;
            } else {
              message.error(res.message);
              return false;
            }
          }}
        >
          <ProFormText width="md" name="userAccount" label="账号" required />
          <ProFormText width="md" name="userName" label="姓名" required />
        </ModalForm>,
      ]}
    />
  );
};

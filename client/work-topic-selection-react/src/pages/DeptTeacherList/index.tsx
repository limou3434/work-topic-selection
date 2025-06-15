import {
  addUserUsingPost,
  deleteUserUsingPost,
  getDeptListUsingPost,
  listUserByPageUsingPost,
  resetPasswordUsingPost,
} from '@/services/work-topic-selection/userController';
import { PlusOutlined } from '@ant-design/icons';
import {
  ActionType,
  ProColumns,
  ProFormSelect,
  ProFormText,
  ProTable,
} from '@ant-design/pro-components';
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
      title: '用户名',
      dataIndex: 'userName',
    },
    {
      title: '系部',
      dataIndex: 'dept',
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
          const paramsWithUserRole = { ...params, userRole: 2 }; // Assuming userRole needs to be passed in params
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
        showSizeChanger: true,
      }}
      dateFormatter="string"
      headerTitle="系部老师账号"
      toolBarRender={() => [
        <React.Fragment key="toolbar">
          <ModalForm<{
            deptName: string;
            userAccount: string;
            userName: string;
          }>
            title="添加主任账号"
            trigger={
              <Button type="primary">
                <PlusOutlined /> 添加主任账号
              </Button>
            }
            autoFocusFirstInput
            modalProps={{
              destroyOnClose: true,
              onCancel: () => console.log('run'),
            }}
            submitTimeout={2000}
            onFinish={async (values) => {
              const addDeptTeacher = { ...values, userRole: 2 }; // Assuming userRole needs to be passed in values
              const res = await addUserUsingPost(addDeptTeacher);
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
            <ProFormSelect
              request={async () => {
                const response = await getDeptListUsingPost({});
                if (response && response.data) {
                  return response.data.map((item) => ({
                    label: item.label,
                    value: item.value,
                  }));
                }
                return [];
              }}
              width="md"
              name="deptName"
              label="系部"
              required={true}
            />
          </ModalForm>
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
          </ModalForm>
        </React.Fragment>,
      ]}
    />
  );
};

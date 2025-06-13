import {
  addUserUsingPost,
  deleteUserUsingPost,
  getDeptListUsingPost,
  getProjectListUsingPost,
  listUserByPageUsingPost, resetPasswordUsingPost,
} from '@/services/bsxt/userController';
import { ActionType, ProColumns, ProFormText, ProTable } from '@ant-design/pro-components';
import React, { useRef, useState } from 'react';
import { Button, message } from 'antd';
import { PlusOutlined, UploadOutlined } from '@ant-design/icons';
import { ModalForm, ProFormSelect, ProFormUploadButton } from '@ant-design/pro-form';
import { uploadFileUsingPost } from '@/services/bsxt/fileController';

type GithubIssueItem = {
  id: number;
  userAccount: string;
  userName: string;
  dept: string;
  project: string;
};

export default () => {
  const actionRef = useRef<ActionType>();
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(5);
  const [total, setTotal] = useState(0);

  const columns: ProColumns<GithubIssueItem>[] = [
    {
      dataIndex: 'id',
      valueType: 'indexBorder',
      width: 48,
    },
    {
      title: '学号',
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
      title: '专业',
      dataIndex: 'project',
    },
    {
      title: '操作',
      valueType: 'option',
      key: 'option',
      render: (text, record, _, action) => [
        <a
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
      //@ts-ignore
      request={async (params = {}, sort, filter) => {
        console.log(sort, filter, params);
        try {
          const paramsWithPagination = { ...params, userRole: 0, pageNum, pageSize };
          const response = await listUserByPageUsingPost(paramsWithPagination);
          const records = response?.data?.records || [];
          const total = response?.data?.total || 0;
          setTotal(total);
          return {
            data: records,
            success: true,
            total: total,
          };
        } catch (error) {
          console.error('Error fetching data:', error);
          return {
            data: [],
            success: false,
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
        pageSize,
        current: pageNum,
        total,
        onChange: (page, size) => {
          setPageNum(page);
          setPageSize(size);
        },
      }}
      dateFormatter="string"
      headerTitle="学生账号"
      toolBarRender={() => [
        <>
          <Button type="primary" key="primary">
            <a href="	https://template-thrive-1322597786.cos.ap-guangzhou.myqcloud.com/%E5%AD%A6%E7%94%9F%E5%AF%BC%E5%85%A5%E6%A8%A1%E6%9D%BF.xlsx " download>
              下载模板
            </a>
          </Button>
          <ModalForm<{
            file: any;
          }>
            title="批量添加学生"
            trigger={
              <Button type="primary">
                <PlusOutlined />
                批量添加学生
              </Button>
            }
            autoFocusFirstInput
            modalProps={{
              destroyOnClose: true,
              onCancel: () => console.log('run'),
            }}
            submitTimeout={2000}
            onFinish={async (values) => {
              const res = await uploadFileUsingPost({ status: 0 }, {}, values.file[0].originFileObj);
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
            <ProFormUploadButton
              width="md"
              name="file"
              label="上传xlsx文件"
              accept=".xlsx"
              max={1}
              required
            >
              <Button icon={<UploadOutlined />}>选择文件</Button>
            </ProFormUploadButton>
          </ModalForm>
          <ModalForm<{
            deptName: string;
            userAccount: string;
            userName: string;
            project: string;
          }>
            title="添加学生"
            trigger={
              <Button type="primary">
                <PlusOutlined />
                添加学生
              </Button>
            }
            autoFocusFirstInput
            modalProps={{
              destroyOnClose: true,
              onCancel: () => console.log('run'),
            }}
            submitTimeout={2000}
            onFinish={async (values) => {
              const res = await addUserUsingPost(values);
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
            <ProFormText
              width="md"
              name="userAccount"
              label="学号"
              required
            />
            <ProFormText
              width="md"
              name="userName"
              label="学生姓名"
              required
            />
            <ProFormSelect
              request={async () => {
                const response = await getDeptListUsingPost({});
                if (response && response.data) {
                  return response.data.map(item => ({
                    label: item.label,
                    value: item.value,
                  }));
                }
                return [];
              }}
              width="md"
              name="deptName"
              label="系部"
              required
            />
            <ProFormSelect
              request={async () => {
                const response = await getProjectListUsingPost({});
                if (response && response.data) {
                  return response.data.map(item => ({
                    label: item.label,
                    value: item.value,
                  }));
                }
                return [];
              }}
              width="md"
              name="project"
              label="专业"
              required
            />
          </ModalForm>
          <ModalForm<{
            userAccount: string;
            userName: string;
          }>
            title="重置密码"
            trigger={
              <Button type="primary">
                <PlusOutlined />
                重置密码
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
            <ProFormText
              width="md"
              name="userAccount"
              label="学号"
              required
            />
            <ProFormText
              width="md"
              name="userName"
              label="学生姓名"
              required
            />
          </ModalForm>
        </>,
      ]}
    />
  );
};

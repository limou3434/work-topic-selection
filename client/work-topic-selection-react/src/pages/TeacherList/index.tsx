// @ts-ignore
import { uploadFileUsingPost } from '@/services/work-topic-selection/fileController';
import {
  addUserUsingPost,
  deleteUserUsingPost,
  getDeptListUsingPost,
  listUserByPageUsingPost,
  resetPasswordUsingPost,
} from '@/services/work-topic-selection/userController';
import { PlusOutlined, UploadOutlined } from '@ant-design/icons';
import { ActionType, ProColumns, ProFormText, ProTable } from '@ant-design/pro-components';
import { ProFormUploadButton } from '@ant-design/pro-form';
import { ModalForm, ProFormSelect } from '@ant-design/pro-form/lib';
import { Button, Dropdown, MenuProps, message } from 'antd';
import { useRef, useState } from 'react';

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
      title: '系部',
      dataIndex: 'dept',
    },
    {
      title: '操作',
      valueType: 'option',
      key: 'option',
      render: (text, record, _, action) => [
        <a
          style={{ color: '#ff4d4f' }}
          key="delete"
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

  const [importModalOpen, setImportModalOpen] = useState(false);

  const items = [
    {
      key: 'downloadTemplate',
      label: '下载批量导入模板',
    },
    {
      key: 'batchAdd',
      label: '根据模板批量导入',
    },
  ];

  const onMenuClick: MenuProps['onClick'] = (e) => {
    if (e.key === 'downloadTemplate') {
      const link = document.createElement('a');
      link.href =
        'https://template-thrive-1322597786.cos.ap-guangzhou.myqcloud.com/%E6%95%99%E5%B8%88%E6%A8%A1%E6%9D%BF.xlsx';
      link.download = '教师账号导入模板.xlsx';
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    } else if (e.key === 'batchAdd') {
      setImportModalOpen(true);
    }
  };

  return (
    <ProTable<GithubIssueItem>
      columns={columns}
      actionRef={actionRef}
      cardBordered
      rowKey="id"
      // 不要自己管理data和total，交给ProTable处理分页
      /* eslint-disable-next-line @typescript-eslint/no-unused-vars */
      request={async (params = {}, sort, filter) => {
        try {
          // 确保分页参数转换正确，比如 current -> pageNum 或 page
          const requestParams = {
            ...params,
            userRole: 1,
            // 如果后端需要page字段，而ProTable默认用current，做转换
            page: params.current,
            pageSize: params.pageSize,
          };
          const response = await listUserByPageUsingPost(requestParams);
          return {
            data: response.data.records,
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
      search={{
        labelWidth: 'auto',
      }}
      form={{
        syncToUrl: (values, type) => (type === 'get' ? values : values),
      }}
      pagination={{
        showSizeChanger: true,
        defaultPageSize: 10,
      }}
      dateFormatter="string"
      headerTitle="教师账号管理"
      toolBarRender={() => [
        <>
          <Dropdown menu={{ items, onClick: onMenuClick }}>
            <Button type="dashed">批量操作</Button>
          </Dropdown>
          <ModalForm<{
            file: any;
          }>
            title="批量添加教师"
            open={importModalOpen}
            onOpenChange={setImportModalOpen}
            autoFocusFirstInput
            modalProps={{
              destroyOnClose: true,
            }}
            submitTimeout={2000}
            onFinish={async (values) => {
              const res = await uploadFileUsingPost(
                { status: 1 },
                {},
                values.file[0].originFileObj,
              );
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
          }>
            title="添加教师账号"
            trigger={
              <Button type="primary">
                <PlusOutlined />
                添加教师账号
              </Button>
            }
            autoFocusFirstInput
            modalProps={{
              destroyOnClose: true,
            }}
            submitTimeout={2000}
            onFinish={async (values) => {
              const addTeacher = { ...values, userRole: 1 };
              const res = await addUserUsingPost(addTeacher);
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
            <ProFormText width="md" name="userAccount" label="工号" required />
            <ProFormText width="md" name="userName" label="姓名" required />
            <ProFormSelect
              request={async () => {
                const response = await getDeptListUsingPost({});
                if (response && response.data) {
                  return response.data.map((item: any) => ({
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
        </>,
      ]}
    />
  );
};

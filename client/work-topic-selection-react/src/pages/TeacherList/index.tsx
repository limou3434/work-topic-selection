// @ts-ignore
import {
  addUserUsingPost,
  deleteUserUsingPost,
  getDeptListUsingPost,
  listUserByPageUsingPost, resetPasswordUsingPost
} from '@/services/work-topic-selection/userController';
import {ActionType, ProColumns, ProFormText} from '@ant-design/pro-components';
import { ProTable } from '@ant-design/pro-components';
import React, { useRef, useState } from 'react';
import { Button, Dropdown, MenuProps, message } from 'antd';
import {ModalForm, ProFormSelect} from "@ant-design/pro-form/lib";
import {EllipsisOutlined, PlusOutlined, UploadOutlined} from "@ant-design/icons";
import {uploadFileUsingPost} from "@/services/work-topic-selection/fileController";
import {ProFormUploadButton} from "@ant-design/pro-form";

type GithubIssueItem = {
  userAccount: string;
  userName: string;
  userProject: string;
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
          style={{ color: '#ff4d4f' }} // Ant Design 默认危险色
          key="editable"
          onClick={async () => {
            const res = await deleteUserUsingPost({userAccount: record.userAccount})
            if (res.code === 0) {
              message.success(res.message)
            } else {
              message.error(res.message)
            }
            action?.reload();
          }}
        >
          删除
        </a>,
      ],
    },
  ];

  const [data, setData] = useState<GithubIssueItem[]>([]);
  const [total, setTotal] = useState<number>(0);

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
    const key = e.key;
    if (key === 'downloadTemplate') {
      const link = document.createElement('a');
      link.href =
        'https://template-thrive-1322597786.cos.ap-guangzhou.myqcloud.com/%E6%95%99%E5%B8%88%E6%A8%A1%E6%9D%BF.xlsx';
      link.download = '教师账号导入模板.xlsx';
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    } else if (key === 'batchAdd') {
      setImportModalOpen(true);
    }
  }

  return (
    <ProTable<GithubIssueItem>
      columns={columns}
      actionRef={actionRef}
      cardBordered
      // @ts-ignore
      request={async (params = {}, sort, filter) => {
        console.log(sort, filter, params);
        try {
          const paramsWithFormName = { ...params, userRole: 1 };
          const response = await listUserByPageUsingPost(paramsWithFormName);
          //@ts-ignore
          setData(response.data.records);
          //@ts-ignore
          setTotal(response.data.total);
          return {
            // @ts-ignore
            data: response.data.records,
            //@ts-ignore
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
        total: total,
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
              onCancel: () => console.log('run'),
            }}
            submitTimeout={2000}
            onFinish={async (values) => {
              console.log(values)
              console.log(values.file[0].originFileObj)
              const res = await uploadFileUsingPost({status:1},{},values.file[0].originFileObj);
              if (res.code === 0) {
                message.success(res.message);
                actionRef?.current?.reload();
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
              required>
              <Button icon={<UploadOutlined />  } >选择文件</Button>
            </ProFormUploadButton>
          </ModalForm>
          <ModalForm<{
            deptName: string;
            userAccount: string;
            userName: string;
          }>
            title="添加教师账号"
            trigger={<Button type="primary">
              <PlusOutlined/>
              添加教师账号
            </Button>}
            autoFocusFirstInput
            modalProps={{
              destroyOnClose: true,
              onCancel: () => console.log('run'),
            }}
            submitTimeout={2000}
            onFinish={async (values) => {
              const addTeacher = {...values,userRole: 1}
              const res = await addUserUsingPost(addTeacher)
              if(res.code===0){
                message.success(res.message)
                actionRef?.current?.reload();
                return true;
              }else {
                message.error(res.message)
              }
            }}
          >
            <ProFormText
              width="md"
              name="userAccount"
              label="工号"
              required={true}
            />
            <ProFormText
              width="md"
              name="userName"
              label="姓名"
              required={true}
            />
            <ProFormSelect
              request={async () => {
                const response = await getDeptListUsingPost({});
                // 确保返回的数据格式符合ProFormSelect的需求
                if (response && response.data) {
                  //@ts-ignore
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
            <ProFormText
              width="md"
              name="userAccount"
              label="账号"
              required
            />
            <ProFormText
              width="md"
              name="userName"
              label="姓名"
              required
            />
          </ModalForm>
        </>,
      ]}
    />
  );
};

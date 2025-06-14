import React, { useRef } from 'react';
import { Button, Dropdown, message } from 'antd';
import { EllipsisOutlined, PlusOutlined } from '@ant-design/icons';
import { ActionType, ProColumns, ProFormSelect, ProFormText, ProTable } from '@ant-design/pro-components';
import { ModalForm } from '@ant-design/pro-form';
import { addProjectUsingPost, deleteProjectUsingPost, getDeptListUsingPost, getProjectUsingPost } from '@/services/work-topic-selection/userController';

type GithubIssueItem = {
  id: number;
  projectName: string;
  deptName: string;
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
      title: '专业',
      dataIndex: 'projectName',
    },
    {
      title: '系部',
      dataIndex: 'deptName',
    },
    {
      title: '操作',
      valueType: 'option',
      key: 'option',
      render: (text, record, _, action) => [
        <a
          key="editable"
          onClick={async () => {
            const res = await deleteProjectUsingPost({ projectName: record.projectName });
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
          // eslint-disable-next-line @typescript-eslint/no-unused-vars
          const { current, pageSize, ...restParams } = params;
          // @ts-ignore
          const response = await getProjectUsingPost(restParams);
          return {
            //@ts-ignore
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
        showSizeChanger: true,
      }}
      dateFormatter="string"
      headerTitle="系部"
      toolBarRender={() => [
        <>
          <ModalForm<{
            projectName: string;
            deptName: string;
          }>
            title="添加专业"
            trigger={
              <Button type="primary">
                <PlusOutlined /> 添加专业
              </Button>
            }
            autoFocusFirstInput
            modalProps={{
              destroyOnClose: true,
              onCancel: () => console.log('run'),
            }}
            submitTimeout={2000}
            onFinish={async (values) => {
              const res = await addProjectUsingPost(values);
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
              name="projectName"
              label="专业名称"
              required={true}
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
              required={true}
            />
          </ModalForm>
          <Dropdown
            key="menu"
            menu={{
              items: [
                {
                  label: '1st item',
                  key: '1',
                },
                {
                  label: '2nd item',
                  key: '2',
                },
                {
                  label: '3rd item',
                  key: '3',
                },
              ],
            }}
          >
            <Button>
              <EllipsisOutlined />
            </Button>
          </Dropdown>
        </>,
      ]}
    />
  );
};


import {
  addDeptUsingPost, deleteDeptUsingPost,
  getDeptUsingPost,
} from '@/services/work-topic-selection/userController';
import {ActionType, ProColumns, ProFormText} from '@ant-design/pro-components';
import { ProTable } from '@ant-design/pro-components';
import React, { useRef, useState } from 'react';
import {Button, Dropdown, message} from "antd";
import {EllipsisOutlined, PlusOutlined} from "@ant-design/icons";
import {ModalForm} from "@ant-design/pro-form/lib";

type GithubIssueItem = {
 deptName:string;
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
      title: '系部',
      dataIndex: 'deptName',

    },
    {
      title: '操作',
      valueType: 'option',
      key: 'option',
      render: (text, record, _,action) => [
        <a
          key="editable"
          onClick={async () => {
            const res = await deleteDeptUsingPost({deptName: record.deptName})
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

  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const [data, setData] = useState<GithubIssueItem[]>([]);


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
          const response = await getDeptUsingPost(paramsWithFormName);
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
      headerTitle="系部"
      toolBarRender={() => [
        <><ModalForm<{
          deptName: string;
        }>
          title="添加系部"
          trigger={<Button type="primary">
            <PlusOutlined/>
            添加系部
          </Button>}
          autoFocusFirstInput
          modalProps={{
            destroyOnClose: true,
            onCancel: () => console.log('run'),
          }}
          submitTimeout={2000}
          onFinish={async (values) => {
            const res = await addDeptUsingPost(values)
            if(res.code===0){
              message.success(res.message)
              actionRef.current?.reload();
              return true;
            }else {
             message.error(res.message)
            }

          }}
        >
          <ProFormText
            width="md"
            name="deptName"
            label="系部名称"
            required={true}
            />
        </ModalForm><Dropdown
          key="menu"
          menu={{
            items: [
              {
                label: '1st item',
                key: '1',
              },
              {
                label: '2nd item',
                key: '1',
              },
              {
                label: '3rd item',
                key: '1',
              },
            ],
          }}
        >
          <Button>
            <EllipsisOutlined/>
          </Button>
        </Dropdown></>,
      ]}
    />
  );
};

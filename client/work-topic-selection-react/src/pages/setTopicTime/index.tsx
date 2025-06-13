import { ProColumns } from '@ant-design/pro-components';
import { ProTable } from '@ant-design/pro-components';
import { Button, message, Space, Table } from 'antd';
import React from "react";
import {
  getTopicListUsingPost,
  setTimeByIdUsingPost
} from "@/services/bsxt/userController";
import { PlusOutlined } from "@ant-design/icons";
import { ModalForm } from "@ant-design/pro-form/lib";
import { ProFormDateTimePicker } from "@ant-design/pro-form";

export type TableListItem = {
  key: number;
  name: string;
  progress: number;
  containers: number;
  callNumber: number;
  creator: string;
  status: string;
  createdAt: number;
  memo: string;
};

const columns: ProColumns<TableListItem>[] = [
  {
    dataIndex: 'id',
    valueType: 'indexBorder',
    width: 48,
  },
  {
    title: '题目',
    dataIndex: 'topic',
  },
  {
    title: '题目类型',
    dataIndex: 'type',
  },
  {
    dataIndex: 'teacherName',
    title: '指导老师',
  },
  {
    title: '系部',
    dataIndex: 'deptName',
  },
  {
    title: '开启时间',
    dataIndex: 'startTime',
    valueType: "dateTime"
  },
  {
    title: '结束时间',
    dataIndex: 'endTime',
    valueType: "dateTime"
  }
];

export default () => {
  return (
    <ProTable<TableListItem>
      columns={columns}
      rowSelection={{
        selections: [Table.SELECTION_ALL, Table.SELECTION_INVERT],
      }}
      //@ts-ignore
      request={async (params = {}, sort, filter) => {
        console.log(sort, filter, params);
        try {
          const response = await getTopicListUsingPost({
            ...params,
            //@ts-ignore
            pageNumber: params.current,
            pageSize: params.pageSize,status:"0"
          });
          return {
            //@ts-ignore
            data: response.data.records, // 确保response.data.records包含你的数据
            //@ts-ignore
            total: response.data.total, // 确保response.data.total包含总记录数
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
      tableAlertRender={({
                           selectedRowKeys,
                           selectedRows,
                           onCleanSelected,
                         }) => {
        console.log(selectedRowKeys, selectedRows);
        return (
          <Space size={24}>
            <span>
              已选 {selectedRowKeys.length} 项
              <a style={{ marginInlineStart: 8 }} onClick={onCleanSelected}>
                取消选择
              </a>
            </span>
          </Space>
        );
      }}
      tableAlertOptionRender={(dataSource) => {
        return (
          <ModalForm<{
            startTime: string;
            endTime: string;
          }>
            title="设置时间"
            trigger={<Button type="primary">
              <PlusOutlined />
              添加时间
            </Button>}
            autoFocusFirstInput
            modalProps={{
              destroyOnClose: true,
              onCancel: () => console.log('run'),
            }}
            submitTimeout={2000}
            onFinish={async (values) => {
              const paramsWithFormName = { ...values, topicList: dataSource.selectedRows };
              const res = await setTimeByIdUsingPost(paramsWithFormName)
              if (res.code === 0) {
                message.success(res.message)
                window.location.reload();
                return true;
              } else {
                message.error(res.message)
              }
            }}
          >
            <ProFormDateTimePicker
              width="md"
              name="startTime"
              label="开始时间"
            />
            <ProFormDateTimePicker
              width="md"
              name="endTime"
              label="结束时间"
            />
          </ModalForm>
        );
      }}
      scroll={{ x: 1300 }}
      options={false}
      search={{
        labelWidth: 'auto',
      }}
      pagination={{
      }}
      rowKey="id" // 确保这个键与数据中的唯一标识符匹配
      headerTitle="设置时间管理"
    />
  );
};

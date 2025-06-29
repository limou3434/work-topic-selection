import { ProColumns, ProTable } from '@ant-design/pro-components';
import { Button, message, Space, Table } from 'antd';
import React, { useState } from "react";
import {
  getTopicListUsingPost,
  setTimeByIdUsingPost
} from "@/services/work-topic-selection/userController";
import { PlusOutlined } from "@ant-design/icons";
import { ModalForm } from "@ant-design/pro-form/lib";
import { ProFormDateTimePicker } from "@ant-design/pro-form";

export type TableListItem = {
  id: number;
  topic: string;
  type: string;
  teacherName: string;
  deptName: string;
  startTime: string;
  endTime: string;
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
  // 分别管理两个分页器
  const [pageNum0, setPageNum0] = useState(1);
  const [pageSize0, setPageSize0] = useState(10);
  const [total0, setTotal0] = useState(0);

  const [pageNum1, setPageNum1] = useState(1);
  const [pageSize1, setPageSize1] = useState(10);
  const [total1, setTotal1] = useState(0);

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 25 }}>
      {/* 第一个列表，status = 0 */}
      <ProTable<TableListItem>
        columns={columns}
        rowSelection={{
          selections: [Table.SELECTION_ALL, Table.SELECTION_INVERT],
        }}
        request={async (params = {}) => {
          const current = params.current || 1;
          const size = params.pageSize || 10;
          setPageNum0(current);
          setPageSize0(size);
          try {
            const res = await getTopicListUsingPost({
              ...params,
              pageNumber: current,
              pageSize: size,
              status: 0,
            });
            const data = res.data?.records || [];
            const total = res.data?.total || 0;
            setTotal0(total);
            return {
              data,
              total,
              success: true,
            };
          } catch (err) {
            console.error(err);
            return { data: [], total: 0, success: false };
          }
        }}
        tableAlertRender={({ selectedRowKeys, onCleanSelected }) => (
          <Space size={24}>
            <span>
              已选 {selectedRowKeys.length} 项
              <a style={{ marginInlineStart: 8 }} onClick={onCleanSelected}>
                取消选择
              </a>
            </span>
          </Space>
        )}
        tableAlertOptionRender={(dataSource) => (
          <ModalForm<{ startTime: string; endTime: string }>
            title="设置时间"
            trigger={
              <Button type="primary">
                <PlusOutlined />
                添加时间
              </Button>
            }
            autoFocusFirstInput
            modalProps={{
              destroyOnClose: true,
            }}
            submitTimeout={2000}
            onFinish={async (values) => {
              const res = await setTimeByIdUsingPost({
                ...values,
                topicList: dataSource.selectedRows,
              });
              if (res.code === 0) {
                message.success(res.message);
                window.location.reload();
                return true;
              } else {
                message.error(res.message);
                return false;
              }
            }}
          >
            <ProFormDateTimePicker name="startTime" label="开始时间" width="md" />
            <ProFormDateTimePicker name="endTime" label="结束时间" width="md" />
          </ModalForm>
        )}
        scroll={{ x: 1300 }}
        options={false}
        search={{ labelWidth: 'auto' }}
        pagination={{
          pageSize: pageSize0,
          current: pageNum0,
          total: total0,
          showSizeChanger: true,
          pageSizeOptions: ['10', '20', '50', '100'],
          onChange: (page, size) => {
            setPageNum0(page);
            setPageSize0(size);
          },
        }}
        rowKey="id"
        headerTitle="设置选题开放时间（这里都是已经通过审核的选题）"
      />

      {/* 第二个列表，status = 1 */}
      <ProTable<TableListItem>
        columns={columns}
        request={async (params = {}) => {
          const current = params.current || 1;
          const size = params.pageSize || 10;
          setPageNum1(current);
          setPageSize1(size);
          try {
            const res = await getTopicListUsingPost({
              ...params,
              pageNumber: current,
              pageSize: size,
              status: 1,
            });
            const data = res.data?.records || [];
            const total = res.data?.total || 0;
            setTotal1(total);
            return {
              data,
              total,
              success: true,
            };
          } catch (err) {
            console.error(err);
            return { data: [], total: 0, success: false };
          }
        }}
        scroll={{ x: 1300 }}
        options={false}
        search={false}
        pagination={{
          pageSize: pageSize1,
          current: pageNum1,
          total: total1,
          showSizeChanger: true,
          pageSizeOptions: ['10', '20', '50', '100'],
          onChange: (page, size) => {
            setPageNum1(page);
            setPageSize1(size);
          },
        }}
        rowKey="id"
        headerTitle="只查看已发布选题（这里都是已经设置开放的选题）"
      />
    </div>
  );
};

import { ProColumns } from '@ant-design/pro-components';
import { ProTable, ModalForm, ProFormText } from '@ant-design/pro-components';
import { message } from 'antd';
import React, { useState, useRef } from "react";
import {
  checkTopicUsingPost,
  getTopicListUsingPost,
} from "@/services/bsxt/userController";

export type TableListItem = {
  id: number;
  key: number;
  topic: string;
  type: string;
  description: string;
  teacherName: string;
  requirement: string;
  status: string;
};

const TopicReviewTable: React.FC = () => {
  const [modalVisible, setModalVisible] = useState(false);
  const [currentRecord, setCurrentRecord] = useState<TableListItem | null>(null);
  const actionRef = useRef();

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
      title: '题目描述',
      dataIndex: 'description',
      valueType: 'textarea',
    },
    {
      title: '指导老师',
      dataIndex: 'teacherName',
    },
    {
      title: '对学生要求',
      dataIndex: 'requirement',
      valueType: 'textarea',
    },
    {
      title: '操作',
      valueType: 'option',
      key: 'option',
      render: (text, record, _, action) => [
        <a
          key="approve"
          onClick={async () => {
            const res = await checkTopicUsingPost({ id: record.id, status: '0' });
            if (res.code === 0) {
              message.success(res.message);
            } else {
              message.error(res.message);
            }
            action?.reload();
          }}
        >
          通过
        </a>,
        <a
          key="reject"
          onClick={() => {
            setCurrentRecord(record);
            setModalVisible(true);
          }}
        >
          打回
        </a>,
      ],
    },
  ];

  return (
    <>
      <ProTable<TableListItem>
        columns={columns}
        actionRef={actionRef} // Reference for reloading the table
        request={async (params = {}, sort, filter) => {
          console.log(sort, filter, params);
          try {
            const paramsWithFormName = { ...params, status: '-1' };
            const response = await getTopicListUsingPost(paramsWithFormName);
            return {
              data: response.data.records,
              total: response.data.total,
            };
          } catch (error) {
            console.error('Error fetching data:', error);
            return {
              data: [],
              total: 0,
            };
          }
        }}
        scroll={{ x: 1300 }}
        options={false}
        search={{
          labelWidth: 'auto',
        }}
        pagination={{
          pageSize: 30,
        }}
        rowKey="id"
        headerTitle="审核题目"
      />
      <ModalForm
        title="打回原因"
        visible={modalVisible}
        onVisibleChange={setModalVisible}
        onFinish={async (values) => {
          if (!currentRecord) return false;
          const res = await checkTopicUsingPost({ id: currentRecord.id, status: '-2', reason: values.reason });
          if (res.code === 0) {
            message.success('打回成功');
            setModalVisible(false);
            setCurrentRecord(null);
            actionRef.current?.reload();
            return true;
          } else {
            message.error('打回失败');
            return false;
          }
        }}
      >
        <ProFormText
          width="md"
          name="reason"
          label="打回原因"
          placeholder="请输入打回原因"
        />
      </ModalForm>
    </>
  );
};

export default TopicReviewTable;

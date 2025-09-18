import { uploadFileTopicUsingPost } from '@/services/work-topic-selection/fileController';
import {
  addTopicUsingPost,
  checkTopicUsingPost, deleteTopicUsingPost,
  getDeptListUsingPost,
  getTeacherUsingPost1,
  getTopicListUsingPost,
  updateTopicUsingPost,
} from '@/services/work-topic-selection/userController';
import { PlusOutlined, UploadOutlined } from '@ant-design/icons';
import { ActionType, ProColumns, ProFormText, ProTable } from '@ant-design/pro-components';
import {
  ModalForm,
  ProFormSelect,
  ProFormTextArea,
  ProFormUploadButton,
} from '@ant-design/pro-form';
import { Button, message } from 'antd';
import { useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import * as async_hooks from 'node:async_hooks';
import { record } from '@umijs/utils/compiled/zod';

type GithubIssueItem = {
  id: number;
  amount?: number;
  deptName?: string;
  deptTeacher?: string;
  description?: string;
  requirement?: string;
  teacherName?: string;
  topic?: string;
  type?: string;
  surplusQuantity?: number;
  status?: number;
  reason?: string;
};

export default () => {
  const actionRef = useRef<ActionType>();
  const navigate = useNavigate();

  const [pageSize, setPageSize] = useState(10);
  const [pageNum, setPageNum] = useState(1);
  const [total, setTotal] = useState(0);

  const columns: ProColumns<GithubIssueItem>[] = [
    {
      title: '序号',
      dataIndex: 'id',
      valueType: 'indexBorder',
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
      title: '对学生的要求',
      dataIndex: 'requirement',
      valueType: 'textarea',
    },
    {
      title: '剩余数量',
      dataIndex: 'surplusQuantity',
      search: false,
      editable: false,
    },
    {
      title: '所属系部',
      dataIndex: 'deptName',
      valueType: 'select',
      request: async () => {
        const response = await getDeptListUsingPost({});
        return (
          response?.data?.map((item) => ({
            label: item.label,
            value: item.value,
          })) || []
        );
      },
    },
    {
      title: '系部主任',
      dataIndex: 'deptTeacher',
      valueType: 'select',
      request: async () => {
        const response = await getTeacherUsingPost1({ userRole: 2 });
        return (
          response?.data?.map((item) => ({
            label: item.label,
            value: item.value,
          })) || []
        );
      },
    },
    {
      title: '状态',
      dataIndex: 'status',
      search: false,
      editable: false,
      render: (_, record) => {
        let color = 'black';
        let text = '未知状态';
        if (record.status === -1) {
          color = 'orange';
          text = '待审核';
        } else if (record.status === 0) {
          color = 'blue';
          text = '没发布（没有设置时间）';
        } else if (record.status === 1) {
          color = 'green';
          text = '已发布';
        } else if (record.status === -2) {
          color = 'red';
          text = '打回';
        }
        return <span style={{ color }}>{text}</span>;
      },
    },
    {
      title: '打回理由',
      dataIndex: 'reason',
      editable: false,
    },
    {
      title: '操作',
      valueType: 'option',
      key: 'option',
      render: (text, record, _, action) => {
        const isPublished = record.status === 1;

        return [
          <a
            key="selectStudent"
            onClick={() => {
              const encodedTopic = encodeURIComponent(record.topic || '');
              navigate(`/topic/teacher/selectStudent/${encodedTopic}`);
            }}
          >
            选择学生
          </a>,
          <a
            key="editable"
            onClick={() => {
              if (isPublished) {
                message.warning('已发布的题目不允许编辑');
                return;
              }
              action?.startEditable?.(record.id);
            }}
            style={{
              color: isPublished ? 'gray' : undefined,
              textDecoration: isPublished ? 'line-through' : undefined,
              cursor: isPublished ? 'not-allowed' : 'pointer',
            }}
          >
            编辑
          </a>,
          <a
            key="resubmit"
            onClick={async () => {
              if (isPublished) {
                message.warning('已发布的题目不允许重新提交审核');
                return;
              }
              const res = await checkTopicUsingPost({ id: record.id, status: -1 });
              if (res.code === 0) {
                message.success('提交成功');
                action?.reload();
              } else {
                message.error(res.message);
              }
            }}
            style={{
              color: isPublished ? 'gray' : undefined,
              textDecoration: isPublished ? 'line-through' : undefined,
              cursor: isPublished ? 'not-allowed' : 'pointer',
            }}
          >
            重新提交审核
          </a>,
        ];
      },
    },
  ];

  return (
    <ProTable<GithubIssueItem>
      columns={columns}
      actionRef={actionRef}
      cardBordered
      request={async (params = {}) => {
        try {
          const current = params.current || 1;
          const size = params.pageSize || 10;
          setPageNum(current);
          setPageSize(size);
          const res = await getTopicListUsingPost({
            ...params,
            userRole: 1,
            current,
            pageSize: size,
          });
          setTotal(res.data?.total || 0);
          return {
            data: res.data?.records || [],
            total: res.data?.total || 0,
            success: true,
          };
        } catch (error) {
          console.error('获取数据错误:', error);
          return {
            data: [],
            total: 0,
            success: false,
          };
        }
      }}
      editable={{
        type: 'multiple',
        onSave: async (key, record) => {
          if (record.status === 1) {
            message.warning('已发布的题目不允许编辑');
            return false;
          }
          const res = await updateTopicUsingPost({ updateTopicListRequests: [record] });
          if (res.code === 0) {
            message.success(res.message);
            return true;
          } else {
            message.error(res.message);
            return false;
          }
        },
        onDelete: async (key, record) => {
          const res = await deleteTopicUsingPost({ id: record?.id });
          if (res.code === 0) {
            message.success(res.message);
            return true;
          }
          else {
            message.error(res.message);
            return false;
          }
        },
        onChange: (editableKeys, editableRows) => {
          // 可选：你可以限制哪些可以编辑
        },
      }}
      columnsState={{
        persistenceKey: 'pro-table-singe-demos',
        persistenceType: 'localStorage',
      }}
      rowKey="id"
      search={{ labelWidth: 'auto' }}
      form={{
        syncToUrl: (values, type) => (type === 'get' ? { ...values } : values),
      }}
      pagination={{
        current: pageNum,
        pageSize,
        total,
        showSizeChanger: true,
        pageSizeOptions: ['10', '20', '50', '100'],
        onChange: (page, size) => {
          setPageNum(page);
          setPageSize(size);
        },
      }}
      dateFormatter="string"
      headerTitle="发布题目和修改题目"
      toolBarRender={() => [
        <>
          <Button type="primary" key="download-template">
            <a
              href="https://wci-1318277926.cos.ap-guangzhou.myqcloud.com/work-topic-selection/%E9%A2%98%E7%9B%AE%E6%89%B9%E9%87%8F%E5%AF%BC%E5%85%A5%E6%A8%A1%E6%9D%BF%20.csv"
              download
            >
              下载模板
            </a>
          </Button>
          <ModalForm<{ file: any }>
            title="批量添加题目"
            trigger={
              <Button type="primary">
                <PlusOutlined /> 批量添加题目
              </Button>
            }
            autoFocusFirstInput
            modalProps={{ destroyOnClose: true }}
            submitTimeout={2000}
            onFinish={async (values) => {
              const res = await uploadFileTopicUsingPost(
                { status: 0 },
                values.file[0].originFileObj,
              );
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
              label="上传 CSV 文件"
              accept=".csv"
              max={1}
              required
            >
              <Button icon={<UploadOutlined />}>选择文件</Button>
            </ProFormUploadButton>
          </ModalForm>

          <ModalForm<{
            topic: string;
            type: string;
            description: string;
            requirement: string;
            teacherName: string;
            deptName: string;
            deptTeacher: string;
          }>
            title="添加题目"
            trigger={
              <Button type="primary">
                <PlusOutlined /> 添加题目
              </Button>
            }
            autoFocusFirstInput
            modalProps={{ destroyOnClose: true }}
            submitTimeout={2000}
            onFinish={async (values) => {
              const res = await addTopicUsingPost(values);
              if (res.code === 0) {
                message.success(res.message);
                actionRef?.current?.reload();
                return true;
              } else {
                message.error(res.message);
              }
            }}
            grid
          >
            <ProFormText width="md" name="topic" label="题目标题" colProps={{ xs: 24, sm: 12 }} required />
            <ProFormText width="md" name="type" label="题目类型" colProps={{ xs: 24, sm: 12 }} required />
            <ProFormTextArea width="md" name="description" label="题目描述" colProps={{ xs: 24, sm: 12 }} required />
            <ProFormTextArea width="md" name="requirement" label="题目要求" colProps={{ xs: 24, sm: 12 }} required />
            <ProFormSelect
              request={async () => {
                const res = await getDeptListUsingPost({});
                return (
                  res?.data?.map((item) => ({
                    label: item.label,
                    value: item.value,
                  })) || []
                );
              }}
              width="md"
              name="deptName"
              label="所属系部"
              required
            />
            <ProFormSelect
              request={async () => {
                const res = await getTeacherUsingPost1({ userRole: 2 });
                return (
                  res?.data?.map((item) => ({
                    label: item.label,
                    value: item.value,
                  })) || []
                );
              }}
              width="md"
              name="deptTeacher"
              label="系部主任"
              required
            />
          </ModalForm>
        </>,
      ]}
    />
  );
};

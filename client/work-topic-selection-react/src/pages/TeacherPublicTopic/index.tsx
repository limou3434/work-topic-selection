import { uploadFileTopicUsingPost } from '@/services/work-topic-selection/fileController';
import {
  addTopicUsingPost,
  checkTopicUsingPost,
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
  status?: string;
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
      title: '对学生要求',
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
              action?.startEditable?.(record.id);
            }}
          >
            编辑
          </a>,
          <a
            key="resubmit"
            onClick={async () => {
              if (isPublished) return; // 已发布不允许点击
              const res = await checkTopicUsingPost({ id: record.id, status: -1 });
              if (res.code === 0) {
                message.success('提交成功');
                action?.reload();
                return true;
              } else {
                message.error(res.message);
                return false;
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
      onDataSourceChange={async (dataSource: GithubIssueItem[]) => {
        const res = await updateTopicUsingPost({ updateTopicListRequests: dataSource });
        if (res.code === 0) {
          message.success(res.message);
          return true;
        } else {
          message.error(res.message);
          return false;
        }
      }}
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
      editable={{ type: 'multiple' }}
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
              href="https://template-thrive-1322597786.cos.ap-guangzhou.myqcloud.com/%E9%A2%98%E7%9B%AE%E5%AF%BC%E5%85%A5%E6%A8%A1%E6%9D%BF%20.xlsx"
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
              label="上传xlsx文件"
              accept=".xlsx"
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
          >
            <ProFormText width="md" name="topic" label="题目" required />
            <ProFormText width="md" name="type" label="题目类型" required />
            <ProFormTextArea width="md" name="description" label="题目描述" required />
            <ProFormTextArea width="md" name="requirement" label="题目要求" required />
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

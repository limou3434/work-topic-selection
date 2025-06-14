import {
  addTopicUsingPost, checkTopicUsingPost,
  getDeptListUsingPost,
  getTeacherUsingPost1,
  getTopicListUsingPost,
  updateTopicUsingPost
} from '@/services/work-topic-selection/userController';
import { ActionType, ProColumns, ProFormText } from '@ant-design/pro-components';
import { ProTable } from '@ant-design/pro-components';
import React, { useRef } from 'react';
import { Button, message } from "antd";
import { ModalForm, ProFormSelect, ProFormTextArea } from "@ant-design/pro-form";
import { PlusOutlined, UploadOutlined } from "@ant-design/icons";
import { uploadFileTopicUsingPost } from "@/services/work-topic-selection/fileController";
import { ProFormUploadButton } from "@ant-design/pro-form";
import { useNavigate } from "react-router-dom";

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
        if (response && response.data) {
          return response.data.map(item => ({
            label: item.label,
            value: item.value,
          }));
        }
        return [];
      },
    },
    {
      title: '系部主任',
      dataIndex: 'deptTeacher',
      valueType: 'select',
      request: async () => {
        const response = await getTeacherUsingPost1({ userRole: 2 });
        if (response && response.data) {
          return response.data.map(item => ({
            label: item.label,
            value: item.value,
          }));
        }
        return [];
      },
    },
    {
      title: '状态',
      dataIndex: 'status',
      search:false,
      render: (_, record) => {
        let color = 'black';
        let text = '未知状态';
        if (record.status === "-1") {
          color = 'orange';
          text = '待审核';
        } else if (record.status === "0") {
          color = 'blue';
          text = '没发布（没有设置时间）';
        } else if (record.status === "1") {
          color = 'green';
          text = '已发布';
        } else if (record.status === "-2") {
          color = 'red';
          text = '打回';
        }
        return <span style={{ color }}>{text}</span>;
      }
    },
    {
      title: '打回理由',
      dataIndex: 'reason',
    },
    {
      title: '操作',
      valueType: 'option',
      key: 'option',
      render: (text, record, _, action) => [
        <a
          key="editable"
          onClick={() => {
            action?.startEditable?.(record.id);
          }}
        >
          编辑
        </a>,
        <a
          key="selectStudent"
          onClick={() => {
            let encodedTopic = encodeURIComponent(record.topic);
            navigate(`/topic/teacher/selectStudent/${encodedTopic}`);
          }}
        >
          选择学生
        </a>,
        <a
          key="selectStudent"
          onClick={async () => {
            const res = await checkTopicUsingPost({id: record.id, status: "-1"});
            if (res.code === 0) {
              message.success("提交成功");
              action?.reload();
              return true;
            } else {
              message.error('提交失败');
              return false;
            }
          }}
        >
          重新提交审核
        </a>,
      ],
    },
  ];

  return (
    <ProTable<GithubIssueItem>
      columns={columns}
      actionRef={actionRef}
      // 修改后的 onDataSourceChange 函数
      onDataSourceChange={async (dataSource: GithubIssueItem[]) => {
        const paramsWithFormName = { updateTopicListRequests: dataSource };
        const res = await updateTopicUsingPost({...paramsWithFormName});
        if (res.code === 0) {
          message.success(res.message);
          return true;
        } else {
          message.error('更新失败');
          return false;
        }
      }}
      cardBordered
      //@ts-ignore
      request={async (params = {}, sort, filter) => {
        console.log(sort, filter, params);
        try {
          const paramsWithFormName = { ...params, userRole: 1 };
          const response = await getTopicListUsingPost({
            ...paramsWithFormName,
            current: params.current,
            pageSize: params.pageSize,
          });
          return {
            //@ts-ignore
            data: response.data.records,
            //@ts-ignore
            total: response.data.total,
          };
        } catch (error) {
          console.error('获取数据错误:', error);
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
      }}
      dateFormatter="string"
      headerTitle="管理员发布题目管理"
      toolBarRender={() => [
        <>
          <Button type="primary" key="primary">
            <a href="https://template-thrive-1322597786.cos.ap-guangzhou.myqcloud.com/%E9%A2%98%E7%9B%AE%E5%AF%BC%E5%85%A5%E6%A8%A1%E6%9D%BF%20.xlsx" download>
              下载模板
            </a>
          </Button>
          <ModalForm<{
            file: any;
          }>
            title="批量添加题目"
            trigger={
              <Button type="primary">
                <PlusOutlined />
                批量添加题目
              </Button>
            }
            autoFocusFirstInput
            modalProps={{
              destroyOnClose: true,
              onCancel: () => console.log('run'),
            }}
            submitTimeout={2000}
            onFinish={async (values) => {
              const res = await uploadFileTopicUsingPost({ status: 0 }, values.file[0].originFileObj);
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
              <Button icon={<UploadOutlined />}>选择文件</Button>
            </ProFormUploadButton>
          </ModalForm>
          <ModalForm<{
            topic: string,
            type: string,
            description: string,
            requirement: string,
            teacherName: string,
            deptName: string,
            deptTeacher: string,
          }>
            title="添加题目"
            trigger={<Button type="primary">
              <PlusOutlined />
              添加题目
            </Button>}
            autoFocusFirstInput
            modalProps={{
              destroyOnClose: true,
              onCancel: () => console.log('run'),
            }}
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
            <ProFormText
              width="md"
              name="topic"
              label="题目"
              required={true}
            />
            <ProFormText
              width="md"
              name="type"
              label="题目类型"
              required={true}
            />
            <ProFormTextArea
              width="md"
              name="description"
              label="题目描述"
              required={true}
            />
            <ProFormTextArea
              width="md"
              name="requirement"
              label="题目要求"
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
              label="所属系部"
              required={true}
            />
            <ProFormSelect
              request={async () => {
                const response = await getTeacherUsingPost1({ userRole: 2 });
                if (response && response.data) {
                  return response.data.map(item => ({
                    label: item.label,
                    value: item.value,
                  }));
                }
                return [];
              }}
              width="md"
              name="deptTeacher"
              label="系部主任"
              required={true}
            />
          </ModalForm>
        </>,
      ]}
    />
  );
};

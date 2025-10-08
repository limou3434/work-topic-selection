import {
  getSelectTopicByIdUsingPost, withdrawUsingPost,
  getTopicLockUsingGet,
} from '@/services/work-topic-selection/userController';
import { LockOutlined, UnlockOutlined } from '@ant-design/icons';
import {ActionType, ProColumns} from '@ant-design/pro-components';
import { ProTable } from '@ant-design/pro-components';
import React, { useEffect, useRef, useState } from 'react';
import {Button, Dropdown, message, Modal} from "antd";
import {EllipsisOutlined} from "@ant-design/icons";
// @ts-ignore
import {useParams } from "react-router-dom"

type GithubIssueItem = {
  userAccount: string;
  userName: string;
  userProject: string;
};

export default () => {
  const actionRef = useRef<ActionType>();
  const {id } = useParams();
  const [topicLocked, setTopicLocked] = useState<boolean>(false);

  // 页面加载时获取初始锁定状态
  useEffect(() => {
    const fetchInitialLockStatus = async () => {
      try {
        const res = await getTopicLockUsingGet();
        if (res && res.code === 0) {
          setTopicLocked(res.data || false);
        }
      } catch (error) {
        console.error('获取初始选题锁定状态失败:', error);
      }
    };

    fetchInitialLockStatus();
  }, []);

  const columns: ProColumns<GithubIssueItem>[] = [
    {
      title: '序号',
      dataIndex: 'id',
      valueType: 'indexBorder',
      width: 48,
    },
    {
      title: '学号',
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
      title: '专业',
      dataIndex: 'project',
    },
    {
      title: '操作',
      valueType: 'option',
      key: 'option',
      render: (text, record, _,action) => [
        <a
          key="select"
          onClick={() => {
            if (topicLocked) {
              return;
            }
            
            // 执行退选前检查锁定状态
            let currentLocked = topicLocked;
            Modal.confirm({
              title: '确认退选',
              content: '确定要退选吗？此操作不可恢复。',
              okText: '确认',
              cancelText: '取消',
              onOk: async () => {
                // 执行退选前再次检查锁定状态
                try {
                  const lockRes = await getTopicLockUsingGet();
                  if (lockRes && lockRes.code === 0) {
                    currentLocked = lockRes.data || false;
                    // 更新状态
                    setTopicLocked(currentLocked);
                  }
                } catch (error) {
                  console.error('获取选题锁定状态失败:', error);
                }
                
                if (currentLocked) {
                  return false; // 阻止对话框关闭
                }
                
                // 执行退选操作
                try {
                  //@ts-ignore
                  const res = await withdrawUsingPost({id: id})
                  if (res.code === 0) {
                    message.success(res.message)
                  } else {
                    message.error(res.message)
                  }
                  action?.reload();
                } catch (error) {
                  message.error('退选操作失败');
                }
              },
            });
          }}
          style={{ 
            color: topicLocked ? '#ccc' : 'inherit', 
            cursor: topicLocked ? 'not-allowed' : 'pointer',
            textDecoration: topicLocked ? 'line-through' : 'none'
          }}
        >
          {topicLocked ? (
            <>
              <LockOutlined /> 锁定
            </>
          ) : (
            <>
              <UnlockOutlined /> 退选
            </>
          )}
        </a>,
      ],
    },
  ];
  return (
    <ProTable<GithubIssueItem>
      columns={columns}
      actionRef={actionRef}
      cardBordered
      // @ts-ignore
      request={async (params = {}, sort, filter) => {
        console.log(sort, filter, params);
        try {
          const values={...params,id:id}
          // @ts-ignore
          const response = await getSelectTopicByIdUsingPost(values);
          return {
            // @ts-ignore
            data: response.data
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
      headerTitle="学生账号"
      toolBarRender={() => [
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
        </Dropdown>,
      ]}
    />
  );
};

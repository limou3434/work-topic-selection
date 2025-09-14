import { getTeacherUsingPost } from '@/services/work-topic-selection/userController';
import { ActionType, ProColumns, ProTable } from '@ant-design/pro-components';
import { Modal } from 'antd';
import { useRef, useState } from 'react';
import TopicTable from '@/components/TopicTable'; // ✅ 改成默认导入

type GithubIssueItem = {
  id: number;
  topicAmount: number;
  teacherName: string;
  selectAmount: number;
};

export default () => {
  const actionRef = useRef<ActionType>();
  const [modalVisible, setModalVisible] = useState(false);
  const [currentTeacher, setCurrentTeacher] = useState<string>('');

  const columns: ProColumns<GithubIssueItem>[] = [
    {
      title: '序号',
      dataIndex: 'id',
      valueType: 'indexBorder',
      width: 48,
    },
    {
      title: '教师姓名',
      dataIndex: 'teacherName',
    },
    {
      title: '题目数量',
      dataIndex: 'topicAmount',
      search: false,
    },
    {
      title: '预选人数',
      dataIndex: 'selectAmount',
      search: false,
    },
    {
      title: '更多操作',
      valueType: 'option',
      render: (_, record) => [
        <a
          key="view"
          onClick={() => {
            setCurrentTeacher(record.teacherName);
            setModalVisible(true);
          }}
        >
          查看题目
        </a>,
      ],
    },
  ];

  return (
    <>
      <ProTable<GithubIssueItem>
        columns={columns}
        actionRef={actionRef}
        cardBordered
        request={async (params = {}) => {
          const response = await getTeacherUsingPost({
            ...params,
            pageNumber: params.current,
            pageSize: params.pageSize,
          });
          return {
            data: response.data?.records || [],
            total: response.data?.total || 0,
            success: true,
          };
        }}
        rowKey="id"
        search={{ labelWidth: 'auto' }}
        pagination={{ pageSize: 10, showSizeChanger: true }}
        headerTitle="预先选题"
      />

      <Modal
        title={`教师 ${currentTeacher} 的题目列表`}
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        footer={null}
        width={1000}
      >
        <TopicTable teacherName={currentTeacher} />
      </Modal>
    </>
  );
};

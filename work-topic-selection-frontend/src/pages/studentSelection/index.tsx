import { getTeacherUsingPost } from '@/services/work-topic-selection/userController';
import { ActionType, ProColumns, ProTable } from '@ant-design/pro-components';
import { Modal } from 'antd';
import { useRef, useState } from 'react';
import TopicTable from '@/components/TopicTable'; // ✅ 改成默认导入
import QRCodeNotification from '@/components/QRCodeNotification';

type GithubIssueItem = {
  topicAmount: number;
  teacherName: string;
  selectAmount: number;
  index: number;
};

export default () => {
  const actionRef = useRef<ActionType>();
  const [modalVisible, setModalVisible] = useState(false);
  const [currentTeacher, setCurrentTeacher] = useState<string>('');
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [total, setTotal] = useState(0);

  const columns: ProColumns<GithubIssueItem>[] = [
    {
      title: '序号',
      dataIndex: 'index',
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
          const currentPage = params.current || 1;
          const currentPageSize = params.pageSize || 10;
          
          // 与StudentList页面保持一致，将所有params传递给后端
          const requestParams = {
            ...params,
            current: currentPage,
            pageSize: currentPageSize,
          };
          
          try {
            const response = await getTeacherUsingPost(requestParams);
            // 检查响应是否有效
            if (!response || response.code !== 0) {
              return {
                data: [],
                total: 0,
                success: false,
              };
            }
            
            // 为数据添加索引字段
            const dataWithIndex = response.data?.records?.map((item, index) => ({
              ...item,
              index: (currentPage - 1) * currentPageSize + index + 1,
            })) || [];
            
            const total = response.data?.total || 0;
            setTotal(total);
            setPageNum(currentPage);
            setPageSize(currentPageSize);
            
            return {
              data: dataWithIndex,
              total: total,
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
        rowKey="teacherName"
        search={{ labelWidth: 'auto' }}
        pagination={{
          pageSize,
          current: pageNum,
          total,
          showSizeChanger: true,
          onChange: (page, size) => {
            setPageNum(page);
            setPageSize(size || 10);
            actionRef.current?.reload();
          },
        }}
        headerTitle="预先选题（每位学生最多预选 10 条题目）"
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
      
      <QRCodeNotification />
    </>
  );
};

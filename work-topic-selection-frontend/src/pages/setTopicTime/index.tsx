import {ProColumns, ProTable} from '@ant-design/pro-components';
import {Button, message, Space, Switch, Table, Tabs} from 'antd';
import React, {useEffect, useState} from "react";
import {
  getCrossTopicStatusUsingGet,
  getSwitchSingleChoiceStatusUsingGet,
  getTopicListUsingPost,
  setCrossTopicStatusUsingPost,
  setSwitchSingleChoiceStatusUsingPost,
  setTimeByIdUsingPost,
  unsetTimeByIdUsingPost
} from "@/services/work-topic-selection/userController";
import {ClockCircleOutlined, EyeOutlined, MinusOutlined, PlusOutlined} from "@ant-design/icons";
import {ModalForm} from "@ant-design/pro-form/lib";
import {ProFormDateRangePicker} from '@ant-design/pro-form';

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

  // 跨系开关状态
  const [crossTopicStatus, setCrossTopicStatus] = useState<boolean>(false);
  // 角色模式开关状态
  const [singleChoiceStatus, setSingleChoiceStatus] = useState<boolean>(false);
  const [loading, setLoading] = useState<boolean>(true);

  // 获取开关状态
  useEffect(() => {
    const fetchStatus = async () => {
      try {
        // 获取跨系开关状态
        const crossRes = await getCrossTopicStatusUsingGet();
        setCrossTopicStatus(crossRes.data || false);

        // 获取角色模式开关状态
        const singleRes = await getSwitchSingleChoiceStatusUsingGet();
        setSingleChoiceStatus(singleRes.data || false);
      } catch (error) {
        console.error("获取开关状态失败:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchStatus().then(() => {
    });
  }, []);

  // 更新跨系开关状态
  const handleCrossTopicStatusChange = async (checked: boolean) => {
    try {
      setLoading(true);
      await setCrossTopicStatusUsingPost({enabled: checked});
      setCrossTopicStatus(checked);
      message.success(`跨系功能已${checked ? "开启" : "关闭"}`);
    } catch (error) {
      console.error("更新跨系开关状态失败:", error);
      message.error("操作失败，请重试");
      // 恢复开关状态
      setCrossTopicStatus(!checked);
    } finally {
      setLoading(false);
    }
  };

  // 更新角色模式开关状态
  const handleSingleChoiceStatusChange = async (checked: boolean) => {
    try {
      setLoading(true);
      await setSwitchSingleChoiceStatusUsingPost({enabled: checked});
      setSingleChoiceStatus(checked);
      message.success(`角色模式已切换为${checked ? "单选" : "多选"}`);
    } catch (error) {
      console.error("更新角色模式开关状态失败:", error);
      message.error("操作失败，请重试");
      // 恢复开关状态
      setSingleChoiceStatus(!checked);
    } finally {
      setLoading(false);
    }
  };

  // @ts-ignore
  return (
    <div style={{display: 'flex', flexDirection: 'column', gap: 25}}>
      <Tabs
        defaultActiveKey="1"
        centered
        items={[
          {
            key: '1',
            label: (
              <span>
                <ClockCircleOutlined/>
                设置选题的开放时间
              </span>
            ),
            children: (
              <>
                <div style={{
                  background: '#ffffff',
                  padding: '12px 16px',
                  borderRadius: 6,
                  marginBottom: 16,
                  boxShadow: 'none',
                }}>
                  <div style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    marginBottom: '12px'
                  }}>
                    <span style={{fontWeight: 500}}>是否允许跨系：</span>
                    <Switch
                      checked={crossTopicStatus}
                      onChange={handleCrossTopicStatusChange}
                      loading={loading}
                    />
                  </div>
                  <div style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                  }}>
                    <span style={{fontWeight: 500}}>单选模式切换：</span>
                    <Switch
                      checked={singleChoiceStatus}
                      onChange={handleSingleChoiceStatusChange}
                      loading={loading}
                    />
                  </div>
                </div>
                <ProTable<TableListItem>
                  columns={columns}
                  rowSelection={{
                    selections: [Table.SELECTION_ALL, Table.SELECTION_INVERT],
                  }}
                  // @ts-ignore
                  request={async (params = {}) => {
                    const current = params.current || 1;
                    const size = params.pageSize || 10;
                    setPageNum0(current);
                    setPageSize0(size);
                    try {
                      const res = await getTopicListUsingPost({
                        ...params,
                        // @ts-ignore
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
                      return {data: [], total: 0, success: false};
                    }
                  }}
                  tableAlertRender={({selectedRowKeys, onCleanSelected}) => (
                    <Space size={24}>
                    <span>
                      已选 {selectedRowKeys.length} 项
                      <a style={{marginInlineStart: 8}} onClick={onCleanSelected}>
                        取消选择
                      </a>
                    </span>
                    </Space>
                  )}
                  tableAlertOptionRender={(dataSource) => (
                    <ModalForm<{ timeRange: [string, string] }>
                      title="设置时间"
                      trigger={
                        <Button type="primary">
                          <PlusOutlined/>
                          添加时间
                        </Button>
                      }
                      autoFocusFirstInput
                      modalProps={{
                        destroyOnClose: true,
                        width: '90%',
                        style: {
                          maxWidth: 520,
                        },
                      }}
                      submitTimeout={2000}
                      onFinish={async (values) => {
                        // 从时间范围中提取开始时间和结束时间
                        const [startTime, endTime] = values.timeRange || [];

                        const res = await setTimeByIdUsingPost({
                          startTime,
                          endTime,
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
                      <ProFormDateRangePicker
                        name="timeRange"
                        label="时间范围"
                        style={{width: '100%'}}
                        placeholder={['开始时间', '结束时间']}
                        // @ts-ignore
                        separator="至"
                      />
                    </ModalForm>
                  )}
                  scroll={{x: 1300}}
                  options={false}
                  search={{labelWidth: 'auto'}}
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
                  headerTitle="设置选题的开放时间"
                />
              </>),
          },
          {
            key: '2',
            label: (
              <span>
                <EyeOutlined/>
                查看已经发布的选题
              </span>
            ),
            children: (
              <ProTable<TableListItem>
                columns={columns}
                rowSelection={{
                  selections: [Table.SELECTION_ALL, Table.SELECTION_INVERT],
                }}
                // @ts-ignore
                request={async (params = {}) => {
                  const current = params.current || 1;
                  const size = params.pageSize || 10;
                  setPageNum1(current);
                  setPageSize1(size);
                  try {
                    const res = await getTopicListUsingPost({
                      ...params,
                      // @ts-ignore
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
                    return {data: [], total: 0, success: false};
                  }
                }}
                tableAlertRender={({selectedRowKeys, onCleanSelected}) => (
                  <Space size={24}>
                    <span>
                      已选 {selectedRowKeys.length} 项
                      <a style={{marginInlineStart: 8}} onClick={onCleanSelected}>
                        取消选择
                      </a>
                    </span>
                  </Space>
                )}
                tableAlertOptionRender={(dataSource) => (
                  <Button
                    type="primary"
                    danger
                    onClick={async () => {
                      const res = await unsetTimeByIdUsingPost({
                        topicList: dataSource.selectedRows,
                      });
                      if (res.code === 0) {
                        message.success(res.message);
                        window.location.reload();
                      } else {
                        message.error(res.message);
                      }
                    }}
                  >
                    <MinusOutlined/>
                    取消发布
                  </Button>
                )}
                scroll={{x: 1300}}
                options={false}
                search={{labelWidth: 'auto'}}
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
                headerTitle="查看已经发布的选题"
              />
            ),
          },
        ]}
      />
    </div>
  );
};

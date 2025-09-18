// @ts-ignore
import {uploadFileUsingPost} from '@/services/work-topic-selection/fileController';
import {
  addUserUsingPost,
  getDeptListUsingPost,
  listUserByPageUsingPost,
  resetPasswordUsingPost,
} from '@/services/work-topic-selection/userController';
import {ExclamationCircleOutlined, PlusOutlined, UploadOutlined} from '@ant-design/icons';
import {ActionType, ProColumns, ProFormText, ProTable} from '@ant-design/pro-components';
import {ProFormUploadButton} from '@ant-design/pro-form';
import {ModalForm, ProFormSelect} from '@ant-design/pro-form/lib';
import {Button, Dropdown, MenuProps, message, Modal} from 'antd';
import {useRef, useState} from 'react';
import {AdjustLimitButton} from "@/components/AdjustLimitButton";

type GithubIssueItem = {
  userAccount: string;
  userName: string;
  dept: string;
};

export default () => {
  const actionRef = useRef<ActionType>();

  const columns: ProColumns<GithubIssueItem>[] = [
    {
      title: '序号',
      dataIndex: 'id',
      valueType: 'indexBorder',
      width: 48,
    },
    {
      title: '工号',
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
      title: '操作',
      valueType: 'option',
      key: 'option',
      render: (_text, record, _, action) => [
        <AdjustLimitButton key={`adjust-${record.userAccount}`} record={record} action={action}/>
      ],
    },
  ];

  const [importModalOpen, setImportModalOpen] = useState(false);

  const items = [
    {
      key: 'downloadTemplate',
      label: '下载批量导入模板',
    },
    {
      key: 'batchAdd',
      label: '根据模板批量导入',
    },
  ];

  const onMenuClick: MenuProps['onClick'] = (e) => {
    if (e.key === 'downloadTemplate') {
      // const link = document.createElement('a');
      // link.href =
      //   'https://wci-1318277926.cos.ap-guangzhou.myqcloud.com/work-topic-selection/%E6%95%99%E5%B8%88%E8%B4%A6%E5%8F%B7%E6%89%B9%E9%87%8F%E5%AF%BC%E5%85%A5%E6%A8%A1%E6%9D%BF.csv';
      // link.download = '教师账号导入模板.csv';
      // document.body.appendChild(link);
      // link.click();
      // document.body.removeChild(link);
      if (e.key === 'downloadTemplate') {
        let second = 5;

        const modal = Modal.confirm({
          title: "提示",
          icon: <ExclamationCircleOutlined />,
          content: "请严格按照模板格式填写 CSV 文件，并且删除模板文件中的示例，不严格按照此规则填写可能导入失败",
          okText: `我已知晓，继续下载 (${second}s)`,
          okButtonProps: { disabled: true },
          cancelText: "取消",
          onCancel: () => {
            // eslint-disable-next-line @typescript-eslint/no-use-before-define
            clearInterval(timer);
          },
          onOk: () => {
            const link = document.createElement('a');
            link.href =
              'https://wci-1318277926.cos.ap-guangzhou.myqcloud.com/work-topic-selection/%E6%95%99%E5%B8%88%E8%B4%A6%E5%8F%B7%E6%89%B9%E9%87%8F%E5%AF%BC%E5%85%A5%E6%A8%A1%E6%9D%BF.csv';
            link.download = '教师账号导入模板.csv';
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            // eslint-disable-next-line @typescript-eslint/no-use-before-define
            clearInterval(timer);
          },
        });

        const timer = setInterval(() => {
          second -= 1;
          modal.update({
            okText: second > 0 ? `我已知晓，继续下载 (${second}s)` : "我已知晓，继续下载",
            okButtonProps: { disabled: second > 0 },
          });
          if (second <= 0) clearInterval(timer);
        }, 1000);
      }
    } else if (e.key === 'batchAdd') {
      setImportModalOpen(true);
    }
  };

  return (
    <ProTable<GithubIssueItem>
      columns={columns}
      actionRef={actionRef}
      cardBordered
      rowKey="id"
      // 不要自己管理data和total，交给ProTable处理分页
      /* eslint-disable-next-line @typescript-eslint/no-unused-vars */
      // @ts-ignore
      request={async (params = {}) => {
        try {
          // 确保分页参数转换正确，比如 current -> pageNum 或 page
          const requestParams = {
            ...params,
            userRole: 1,
            // 如果后端需要page字段，而ProTable默认用current，做转换
            page: params.current,
            pageSize: params.pageSize,
          };
          const response = await listUserByPageUsingPost(requestParams);
          return {
            // @ts-ignore
            data: response.data.records,
            // @ts-ignore
            total: response.data.total,
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
      editable={{
        type: 'multiple',
      }}
      columnsState={{
        persistenceKey: 'pro-table-singe-demos',
        persistenceType: 'localStorage',
      }}
      search={{
        labelWidth: 'auto',
      }}
      form={{
        syncToUrl: (values, type) => (type === 'get' ? values : values),
      }}
      pagination={{
        showSizeChanger: true,
        defaultPageSize: 10,
      }}
      dateFormatter="string"
      headerTitle="教师账号管理"
      toolBarRender={() => [
        <>
          <Dropdown menu={{items, onClick: onMenuClick}}>
            <Button type="dashed">批量操作</Button>
          </Dropdown>
          <ModalForm<{
            file: any;
          }>
            title="批量添加教师"
            open={importModalOpen}
            onOpenChange={setImportModalOpen}
            autoFocusFirstInput
            modalProps={{
              destroyOnClose: true,
            }}
            submitTimeout={2000}
            onFinish={async (values) => {
              const res = await uploadFileUsingPost(
                {status: 1},
                {},
                values.file[0].originFileObj,
              );
              if (res.code === 0) {
                message.success(res.message);
                actionRef.current?.reload();
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
              <Button icon={<UploadOutlined/>}>选择文件</Button>
            </ProFormUploadButton>
          </ModalForm>
          <ModalForm<{
            deptName: string;
            userAccount: string;
            userName: string;
          }>
            title="添加教师账号"
            trigger={
              <Button type="primary">
                <PlusOutlined/>
                添加教师账号
              </Button>
            }
            autoFocusFirstInput
            modalProps={{
              destroyOnClose: true,
            }}
            submitTimeout={2000}
            onFinish={async (values) => {
              const addTeacher = {...values, userRole: 1};
              const res = await addUserUsingPost(addTeacher);
              if (res.code === 0) {
                message.success(res.message);
                actionRef.current?.reload();
                return true;
              } else {
                message.error(res.message);
                return false;
              }
            }}
          >
            <ProFormText width="md" name="userAccount" label="工号" required/>
            <ProFormText width="md" name="userName" label="姓名" required/>
            <ProFormSelect
              request={async () => {
                const response = await getDeptListUsingPost({});
                if (response && response.data) {
                  return response.data.map((item: any) => ({
                    label: item.label,
                    value: item.value,
                  }));
                }
                return [];
              }}
              width="md"
              name="deptName"
              label="系部"
              required
            />
          </ModalForm>
          <ModalForm<{
            userAccount: string;
            userName: string;
          }>
            title="重置账号密码"
            trigger={
              <Button type="primary" ghost>
                <PlusOutlined/>
                重置账号密码
              </Button>
            }
            autoFocusFirstInput
            modalProps={{
              destroyOnClose: true,
            }}
            submitTimeout={2000}
            onFinish={async (values) => {
              const res = await resetPasswordUsingPost(values);
              if (res.code === 0) {
                message.success(res.message);
                actionRef.current?.reload();
                return true;
              } else {
                message.error(res.message);
                return false;
              }
            }}
          >
            <ProFormText width="md" name="userAccount" label="账号" required/>
            <ProFormText width="md" name="userName" label="姓名" required/>
          </ModalForm>
        </>,
      ]}
    />
  );
};

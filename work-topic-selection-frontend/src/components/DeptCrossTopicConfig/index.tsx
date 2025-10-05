import React, { useEffect, useState } from 'react';
import { Button, Card, message, Select, Transfer } from 'antd';
import type { TransferItem } from 'antd/es/transfer';
import {
  getDeptConfigUsingGet,
  getDeptListUsingPost,
  setDeptConfigUsingPost,
} from '@/services/work-topic-selection/userController';

interface DeptConfig {
  enableSelectDeptsList?: Record<string, string[]>;
}

const DeptCrossTopicConfig: React.FC = () => {
  // 系部列表
  const [deptList, setDeptList] = useState<API.DeptVO[]>([]);
  // 选中的系部
  const [selectedDept, setSelectedDept] = useState<string>('');
  // 穿梭框数据
  const [targetKeys, setTargetKeys] = useState<string[]>([]);
  const [mockData, setMockData] = useState<TransferItem[]>([]);
  // 当前配置
  const [deptConfig, setDeptConfig] = useState<DeptConfig>({});

  // 获取所有系部列表
  const fetchDeptList = async () => {
    try {
      const res = await getDeptListUsingPost({});
      if (res.code === 0) {
        setDeptList(res.data || []);
        // 初始化mockData
        const data = (res.data || []).map((dept: API.DeptVO) => ({
          key: dept.value,
          title: dept.label,
          description: `可选系部: ${dept.label}`,
        }));
        setMockData(data);
      }
    } catch (error) {
      console.error('获取系部列表失败:', error);
      message.error('获取系部列表失败');
    }
  };

  // 获取跨系配置
  const fetchDeptConfig = async () => {
    try {
      const res = await getDeptConfigUsingGet();
      if (res.code === 0) {
        setDeptConfig(res.data || {});
        // 如果已选择系部，更新穿梭框目标项
        if (selectedDept && res.data?.enableSelectDeptsList?.[selectedDept]) {
          setTargetKeys(res.data.enableSelectDeptsList[selectedDept]);
        }
      }
    } catch (error) {
      console.error('获取跨系配置失败:', error);
      message.error('获取跨系配置失败');
    }
  };

  // 初始化数据
  useEffect(() => {
    fetchDeptList();
    fetchDeptConfig();
  }, []);

  // 当选中的系部改变时，更新穿梭框目标项
  useEffect(() => {
    if (selectedDept && deptConfig?.enableSelectDeptsList?.[selectedDept]) {
      setTargetKeys(deptConfig.enableSelectDeptsList[selectedDept]);
    } else {
      setTargetKeys([]);
    }
  }, [selectedDept, deptConfig]);

  // 穿梭框变化处理
  const handleChange = (nextTargetKeys: string[]) => {
    setTargetKeys(nextTargetKeys);
  };

  // 设置规则
  const handleSetRules = async () => {
    try {
      // 构造配置数据
      const enableSelectDeptsList: Record<string, string[]> = {};

      // 如果有选中的系部，则更新配置
      if (selectedDept) {
        // 先复制现有的配置
        Object.keys(deptConfig?.enableSelectDeptsList || {}).forEach((deptName) => {
          if (deptName !== selectedDept) {
            enableSelectDeptsList[deptName] = deptConfig.enableSelectDeptsList![deptName];
          }
        });
        // 更新当前选中系部的配置
        enableSelectDeptsList[selectedDept] = targetKeys;
      } else {
        // 如果没有选中系部，保持现有配置
        Object.assign(enableSelectDeptsList, deptConfig?.enableSelectDeptsList || {});
      }

      const res = await setDeptConfigUsingPost({
        enableSelectDeptsList,
      });

      if (res.code === 0) {
        message.success('设置成功');
        // 更新本地配置状态
        setDeptConfig({ enableSelectDeptsList });
      } else {
        message.error(res.message || '设置失败');
      }
    } catch (error) {
      console.error('设置规则失败:', error);
      message.error('设置规则失败');
    }
  };

  return (
    <Card bordered={false} style={{ marginBottom: 24 }}>
      <div style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
        {/* 选择系部下拉框和设置规则按钮 */}
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
            <span style={{ fontWeight: 500, width: 100 }}>配置跨选规则：</span>
            <Select
              style={{ width: 300 }}
              placeholder="请选择系部"
              value={selectedDept || undefined}
              onChange={(value) => setSelectedDept(value)}
              options={deptList.map((dept) => ({
                label: dept.label,
                value: dept.value,
              }))}
            />
          </div>
          <Button type="primary" onClick={handleSetRules}>
            设置规则
          </Button>
        </div>

        {/* 穿梭框 */}
        {selectedDept && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
            <div style={{ fontWeight: 500 }}>
              配置 {selectedDept} 系可选其他系部:
            </div>
            <Transfer
              dataSource={mockData}
              titles={['目标系部', '可选系部']}
              targetKeys={targetKeys}
              onChange={handleChange}
              render={(item) => item.title}
              listStyle={{
                width: 300,
                height: 300,
              }}
            />
          </div>
        )}
      </div>
    </Card>
  );
};

export default DeptCrossTopicConfig;

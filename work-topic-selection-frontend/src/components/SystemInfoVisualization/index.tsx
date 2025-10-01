import { getSystemInfoUsingGet } from '@/services/work-topic-selection/userController';
import { StatisticCard } from '@ant-design/pro-components';
import { useEffect, useState } from 'react';
import ReactECharts from 'echarts-for-react';

const SystemInfoVisualization = () => {
  const [systemInfo, setSystemInfo] = useState({
    memoryUsage: '0 GB/0 GB',
    cpuUsage: '0%',
    diskUsage: '0 GB/0 GB',
    jvmMemoryUsage: '0 MB/0 GB',
  });

  const fetchSystemInfo = async () => {
    try {
      const response = await getSystemInfoUsingGet();
      if (response.code === 0) {
        // @ts-ignore
        setSystemInfo(response.data);
      } else {
        console.error('系统信息获取失败:', response.message);
      }
    } catch (error) {
      console.error('系统信息获取失败:', error);
    }
  };

  useEffect(() => {
    // 立即获取一次数据
    fetchSystemInfo();
    
    // 每5秒刷新一次数据
    const interval = setInterval(fetchSystemInfo, 5000);
    
    // 清理定时器
    return () => clearInterval(interval);
  }, []);

  // 解析使用率数据（除了CPU使用率）
  const parseUsage = (usage: string) => {
    // 处理空值或未定义值的情况
    if (!usage) {
      return { used: 0, total: 0, unit: '' };
    }
    
    const [used, total] = usage.split('/');
    const usedValue = parseFloat(used) || 0;
    
    // 处理total为undefined的情况
    if (!total) {
      return { used: usedValue, total: 0, unit: '' };
    }
    
    const totalValue = parseFloat(total) || 0;
    // 提取单位
    const totalUnit = total.replace(/[\d.]/g, '').trim() || '';
    const usedUnit = used.replace(/[\d.]/g, '').trim() || '';
    
    // 单位转换，将所有值转换为MB进行计算（除了CPU使用率）
    // 根据后端formatSize方法的逻辑，单位可能是B, KB, MB, GB, TB, PB, EB
    const convertToMB = (value: number, unit: string) => {
      switch (unit.toUpperCase()) { // 转换为大写以处理可能的大小写不一致
        case 'EB':
        case 'E':
          return value * 1024 * 1024 * 1024 * 1024 * 1024;
        case 'PB':
        case 'P':
          return value * 1024 * 1024 * 1024 * 1024;
        case 'TB':
        case 'T':
          return value * 1024 * 1024 * 1024;
        case 'GB':
        case 'G':
          return value * 1024;
        case 'MB':
        case 'M':
          return value;
        case 'KB':
        case 'K':
          return value / 1024;
        case 'B':
          return value / (1024 * 1024);
        default:
          return value;
      }
    };
    
    const usedInMB = convertToMB(usedValue, usedUnit);
    const totalInMB = convertToMB(totalValue, totalUnit);
    
    return { 
      used: usedValue, // 保持原始值用于显示
      total: totalValue, // 保持原始值用于显示
      usedUnit: usedUnit,
      totalUnit: totalUnit,
      usedInMB: usedInMB, // 转换后的值用于计算
      totalInMB: totalInMB  // 转换后的值用于计算
    };
  };

  // 解析JVM内存使用率数据（特殊处理）
  const parseJvmUsage = (usage: string) => {
    // 处理空值或未定义值的情况
    if (!usage) {
      return { used: 0, total: 0, unit: '' };
    }
    
    const [used, total] = usage.split('/');
    const usedValue = parseFloat(used) || 0;
    
    // 处理total为undefined的情况
    if (!total) {
      return { used: usedValue, total: 0, unit: '' };
    }
    
    const totalValue = parseFloat(total) || 0;
    // 提取总内存的单位
    const totalUnit = total.replace(/[\d.]/g, '').trim() || '';
    // 提取已使用内存的单位
    const usedUnit = used.replace(/[\d.]/g, '').trim() || '';
    
    // 单位转换，将所有值转换为MB进行计算
    // 根据后端formatSize方法的逻辑，单位可能是B, KB, MB, GB, TB, PB, EB
    const convertToMB = (value: number, unit: string) => {
      switch (unit.toUpperCase()) { // 转换为大写以处理可能的大小写不一致
        case 'EB':
        case 'E':
          return value * 1024 * 1024 * 1024 * 1024 * 1024;
        case 'PB':
        case 'P':
          return value * 1024 * 1024 * 1024 * 1024;
        case 'TB':
        case 'T':
          return value * 1024 * 1024 * 1024;
        case 'GB':
        case 'G':
          return value * 1024;
        case 'MB':
        case 'M':
          return value;
        case 'KB':
        case 'K':
          return value / 1024;
        case 'B':
          return value / (1024 * 1024);
        default:
          return value;
      }
    };
    
    const usedInMB = convertToMB(usedValue, usedUnit);
    const totalInMB = convertToMB(totalValue, totalUnit);
    
    return { 
      used: usedValue, // 保持原始值用于显示
      total: totalValue, // 保持原始值用于显示
      usedUnit: usedUnit,
      totalUnit: totalUnit,
      usedInMB: usedInMB, // 转换后的值用于计算
      totalInMB: totalInMB  // 转换后的值用于计算
    };
  };

  // 计算百分比
  const calculatePercentage = (used: number, total: number) => {
    return total ? Math.min((used / total) * 100, 100) : 0;
  };

  const memoryData = parseUsage(systemInfo.memoryUsage);
  const cpuData = parseUsage(systemInfo.cpuUsage);
  const diskData = parseUsage(systemInfo.diskUsage);
  const jvmData = parseJvmUsage(systemInfo.jvmMemoryUsage);

  const memoryPercentage = calculatePercentage(memoryData.usedInMB, memoryData.totalInMB);
  const cpuPercentage = calculatePercentage(cpuData.used, cpuData.total);
  const diskPercentage = calculatePercentage(diskData.usedInMB, diskData.totalInMB);
  const jvmPercentage = calculatePercentage(jvmData.usedInMB, jvmData.totalInMB);

  // 环形图配置
  const createGaugeOption = (percentage: number, title: string, color: string) => ({
    tooltip: {
      formatter: '{a} <br/>{b} : {c}%',
    },
    series: [
      {
        name: title,
        type: 'gauge',
        startAngle: 180,
        endAngle: 0,
        center: ['50%', '80%'],
        radius: '90%',
        min: 0,
        max: 100,
        splitNumber: 4,
        axisLine: {
          lineStyle: {
            width: 6,
            color: [
              [percentage / 100, color],
              [1, '#ddd'],
            ],
          },
        },
        pointer: {
          icon: 'path://M12.8,0.7l12,40.1H0.7L12.8,0.7z',
          length: '12%',
          width: 20,
          offsetCenter: [0, '-60%'],
          itemStyle: {
            color: 'auto',
          },
        },
        axisTick: {
          length: 12,
          lineStyle: {
            color: 'auto',
            width: 2,
          },
        },
        splitLine: {
          length: 20,
          lineStyle: {
            color: 'auto',
            width: 5,
          },
        },
        axisLabel: {
          color: '#464646',
          fontSize: 12,
          distance: -60,
          rotate: 'tangential',
          formatter: function (value: number) {
            if (value === 0) {
              return '0%';
            }
            if (value === 100) {
              return '100%';
            }
            return '';
          },
        },
        title: {
          offsetCenter: [0, '-20%'],
          fontSize: 14,
        },
        detail: {
          fontSize: 24,
          offsetCenter: [0, '-5%'],
          valueAnimation: true,
          formatter: function (value: number) {
            return Math.round(value) + '%';
          },
          color: 'inherit',
        },
        data: [{ value: percentage, name: title }],
      },
    ],
  });

  return (
  <div
    style={{
      display: 'flex',
      flexWrap: 'wrap',
      justifyContent: 'center',
      gap: 24,
      marginTop: 24,
      marginBottom: 24,
    }}
  >
    {/* 第一行：内存使用率和CPU使用率 */}
    <div
      style={{
        display: 'flex',
        flexWrap: 'wrap',
        justifyContent: 'center',
        gap: 24,
        width: '100%',
      }}
    >
      {[
        {
          title: '内存使用率',
          value: `${memoryData.used.toFixed(1)}${memoryData.usedUnit}/${memoryData.total.toFixed(1)}${memoryData.totalUnit}`,
          percentage: memoryPercentage,
          color: '#5ee7e7',
        },
        {
          title: 'CPU使用率',
          value: `${cpuData.used.toFixed(2)}${cpuData.unit}`,
          percentage: cpuData.used, // CPU使用率直接使用used值作为百分比
          color: '#eee86e',
        },
      ].map(({ title, value, percentage, color }) => (
        <div
          key={title}
          style={{
            flex: '0 0 calc(50% - 12px)',
            minWidth: 280,
            maxWidth: 500,
            background: '#fff',
            borderRadius: 12,
            overflow: 'hidden',
            boxShadow: '0 2px 8px rgba(0, 0, 0, 0.08)',
            transition: 'transform 0.3s ease',
          }}
          onMouseEnter={(e) => {
            e.currentTarget.style.transform = 'scale(1.02)';
          }}
          onMouseLeave={(e) => {
            e.currentTarget.style.transform = 'scale(1)';
          }}
        >
          <StatisticCard
            statistic={{ title, value }}
            chart={
              <ReactECharts
                option={createGaugeOption(percentage, title, color)}
                style={{
                  width: '100%',
                  height: '40vw',
                  maxHeight: 400,
                  minHeight: 240,
                  marginTop: 12,
                }}
                opts={{ renderer: 'svg' }}
                notMerge={true}
                lazyUpdate={true}
              />
            }
            bodyStyle={{ padding: 24 }}
          />
        </div>
      ))}
    </div>

    {/* 第二行：磁盘使用率和JVM内存使用率 */}
    <div
      style={{
        display: 'flex',
        flexWrap: 'wrap',
        justifyContent: 'center',
        gap: 24,
        width: '100%',
      }}
    >
      {[
        {
          title: '磁盘使用率',
          value: `${diskData.used.toFixed(1)}${diskData.usedUnit}/${diskData.total.toFixed(1)}${diskData.totalUnit}`,
          percentage: diskPercentage,
          color: '#d18aec',
        },
        {
          title: 'JVM内存使用率',
          value: `${jvmData.used.toFixed(1)}${jvmData.usedUnit}/${jvmData.total.toFixed(1)}${jvmData.totalUnit}`,
          percentage: jvmPercentage,
          color: '#5d9fea',
        },
      ].map(({ title, value, percentage, color }) => (
        <div
          key={title}
          style={{
            flex: '0 0 calc(50% - 12px)',
            minWidth: 280,
            maxWidth: 500,
            background: '#fff',
            borderRadius: 12,
            overflow: 'hidden',
            boxShadow: '0 2px 8px rgba(0, 0, 0, 0.08)',
            transition: 'transform 0.3s ease',
          }}
          onMouseEnter={(e) => {
            e.currentTarget.style.transform = 'scale(1.02)';
          }}
          onMouseLeave={(e) => {
            e.currentTarget.style.transform = 'scale(1)';
          }}
        >
          <StatisticCard
            statistic={{ title, value }}
            chart={
              <ReactECharts
                option={createGaugeOption(percentage, title, color)}
                style={{
                  width: '100%',
                  height: '40vw',
                  maxHeight: 400,
                  minHeight: 240,
                  marginTop: 12,
                }}
                opts={{ renderer: 'svg' }}
                notMerge={true}
                lazyUpdate={true}
              />
            }
            bodyStyle={{ padding: 24 }}
          />
        </div>
      ))}
    </div>
  </div>
);
};

export default SystemInfoVisualization;
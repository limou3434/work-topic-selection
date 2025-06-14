import { getSelectTopicStudentListCsvUsingPost } from '@/services/bsxt/fileController';
import { getSelectTopicSituationUsingPost } from '@/services/bsxt/userController';
import { StatisticCard } from '@ant-design/pro-components';
import { Button, message } from 'antd';
import { useForm } from 'antd/es/form/Form';
import ReactECharts from 'echarts-for-react';
import RcResizeObserver from 'rc-resize-observer';
import { useEffect, useState } from 'react';

const { Statistic } = StatisticCard;

const YourComponent = () => {
  const [form] = useForm();
  const [data, setData] = useState({ amount: 0, selectAmount: 0, unselectAmount: 0 });

  useEffect(() => {
    (async () => {
      try {
        const response = await getSelectTopicSituationUsingPost();
        if (response.code === 0) {
          // @ts-ignore
          setData(response.data);
        } else {
          console.error('数据获取失败:', response.message);
        }
      } catch (error) {
        console.error('数据获取失败:', error);
      }
    })();
  }, []);

  const totalChartOption = {
    series: [
      {
        name: '总人数',
        type: 'pie',
        radius: [40, 140],
        center: ['50%', '50%'],
        roseType: 'area',
        itemStyle: { borderRadius: 8 },
        data: [
          { value: data.amount, name: '总人数', itemStyle: { color: '#5ee7e7' } },
          { value: data.selectAmount, name: '已经选题人数', itemStyle: { color: '#eee86e' } },
          { value: data.unselectAmount, name: '没有选题人数', itemStyle: { color: '#d18aec' } },
        ],
      },
    ],
  };

  const selectedChartOption = {
    series: [
      {
        name: '人数',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2,
        },
        label: { show: false, position: 'center' },
        emphasis: {
          label: {
            show: true,
            fontSize: 32,
            fontWeight: 'bold',
          },
        },
        labelLine: { show: false },
        data: [
          { value: data.selectAmount, name: '已经选题人数', itemStyle: { color: '#5d9fea' } },
          { value: data.unselectAmount, name: '没有选题人数', itemStyle: { color: '#aeee62' } },
        ],
      },
    ],
  };

  const exportSelectedStudents = async () => {
    try {
      const response = await getSelectTopicStudentListCsvUsingPost();
      const blob = new Blob([response], { type: 'text/csv;charset=utf-8' });
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = '已选题学生列表.csv';
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      window.URL.revokeObjectURL(url);
    } catch (error) {
      console.error('导出失败:', error);
      message.error('导出失败，请稍后重试！');
    }
  };

  return (
    <div style={{ padding: 24, maxWidth: 1600, margin: '0 auto' }}>
      <div
        style={{
          marginBottom: 24,
          display: 'flex',
          justifyContent: 'space-between',
          flexWrap: 'wrap',
          gap: 12,
        }}
      >
        <Button type="primary" onClick={exportSelectedStudents}>
          导出已选题学生名单
        </Button>
      </div>

      <RcResizeObserver onResize={() => {}}>
        <div
          style={{
            display: 'flex',
            flexWrap: 'wrap',
            justifyContent: 'center',
            gap: 24,
          }}
        >
          {[
            {
              title: '本次选题情况',
              value: data.amount,
              chartOption: totalChartOption,
              description: null,
            },
            {
              title: '选题人数分析',
              value: data.selectAmount,
              chartOption: selectedChartOption,
              description: (
                <Statistic
                  title="占比"
                  value={`${
                    data.amount ? ((data.selectAmount / data.amount) * 100).toFixed(1) : 0
                  }%`}
                />
              ),
            },
          ].map(({ title, value, chartOption, description }) => (
            <div
              key={title}
              style={{
                width: 500,
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
                statistic={{ title, value, description }}
                chart={
                  <ReactECharts
                    option={chartOption}
                    style={{ height: 400, width: '100%', marginTop: 12 }}
                  />
                }
                bodyStyle={{ padding: 24 }}
              />
            </div>
          ))}
        </div>
      </RcResizeObserver>
    </div>
  );
};

export default YourComponent;

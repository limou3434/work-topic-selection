import { StatisticCard } from '@ant-design/pro-components';
import RcResizeObserver from 'rc-resize-observer';
import ReactECharts from 'echarts-for-react';
import React, { useState, useEffect } from 'react';
import { getSelectTopicSituationUsingPost } from '@/services/bsxt/userController';
import { useNavigate } from 'react-router-dom';
import { Button, message } from 'antd';
import {
  getSelectTopicStudentListCsvUsingPost,
} from "@/services/bsxt/fileController";

const { Statistic, Divider } = StatisticCard;

const YourComponent = () => {
  const [responsive, setResponsive] = useState(false);
  const [data, setData] = useState({ amount: 0, selectAmount: 0, unselectAmount: 0 });
  const navigate = useNavigate();

  useEffect(() => {
    const fetchData = async () => {
      try {
        const response = await getSelectTopicSituationUsingPost();
        if (response.code === 0) {
          //@ts-ignore
          setData(response.data);
        } else {
          console.error('数据获取失败:', response.message);
        }
      } catch (error) {
        console.error('数据获取失败:', error);
      }
    };

    fetchData();
  }, []);

  const totalChartOption = {
    legend: {
      top: 'bottom',
    },
    toolbox: {
      show: true,
      feature: {
        mark: { show: true },
        dataView: { show: true, readOnly: false },
        restore: { show: true },
        saveAsImage: { show: true },
      },
    },
    series: [
      {
        name: '总人数',
        type: 'pie',
        radius: [40, 140],
        center: ['50%', '50%'],
        roseType: 'area',
        itemStyle: {
          borderRadius: 8,
        },
        data: [
          { value: data.amount, name: '系部总人数', itemStyle: { color: '#5ee7e7' } },
          { value: data.selectAmount, name: '系部已经选题人数', itemStyle: { color: '#eee86e' } },
          { value: data.unselectAmount, name: '系部没有选题人数', itemStyle: { color: '#d18aec' } },
        ],
      },
    ],
  };

  const selectedChartOption = {
    tooltip: {
      trigger: 'item',
    },
    legend: {
      top: '5%',
      left: 'center',
    },
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
        label: {
          show: false,
          position: 'center',
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 40,
            fontWeight: 'bold',
          },
        },
        labelLine: {
          show: false,
        },
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
      a.download = '已选题学生列表.csv'; // 修改文件名和后缀
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      window.URL.revokeObjectURL(url);
    } catch (error) {
      console.error('导出失败:', error);
      message.error('导出失败，请稍后重试！');
    }
  };

  const unselectedChartOption = {
    tooltip: {
      trigger: 'item',
    },
    legend: {
      top: '5%',
      left: 'center',
    },
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
        label: {
          show: false,
          position: 'center',
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 40,
            fontWeight: 'bold',
          },
        },
        labelLine: {
          show: false,
        },
        data: [
          { value: data.selectAmount, name: '已经选题人数', itemStyle: { color: '#f3c734' } },
          { value: data.unselectAmount, name: '没有选题人数', itemStyle: { color: '#ec5c60' } },
        ],
      },
    ],
  };

  return (
    <div
      style={{
        position: 'relative', // 确保子元素使用绝对定位时相对于该元素定位
        height: '80vh',
        width: '80vw',
        display: 'flex',
        flexDirection: 'column',
        padding: '20px',
        boxSizing: 'border-box',
      }}
    >
      <Button
        type="primary"
        size="large"
        onClick={exportSelectedStudents}
        style={{
          position: 'absolute',
          top: 20,
          right: 20,
          zIndex: 1, // 确保按钮在其他内容之上
        }}
      >
        导出已选题学生名单
      </Button>
      <RcResizeObserver
        key="resize-observer"
        onResize={(offset) => {
          setResponsive(offset.width < 599);
        }}
      >
        <StatisticCard.Group direction={responsive ? 'column' : 'row'}>
          <StatisticCard
            statistic={{
              title: '系部总人数',
              value: data.amount,
            }}
            chart={<ReactECharts option={totalChartOption} style={{ height: '500px', width: '100%' }} />}
          />
          <Divider type={responsive ? 'horizontal' : 'vertical'} />
          <StatisticCard
            statistic={{
              title: '系部已经选题人数',
              value: data.selectAmount,
              description: <Statistic title="占比" value={`${((data.selectAmount / data.amount) * 100).toFixed(1)}%`} />,
            }}
            chart={<ReactECharts option={selectedChartOption} style={{ height: '500px', width: '100%' }} />}
          />
          <Divider type={responsive ? 'horizontal' : 'vertical'} />
          <StatisticCard
            statistic={{
              title: '系部没有选题人数',
              value: data.unselectAmount,
              description: <Statistic title="占比" value={`${((data.unselectAmount / data.amount) * 100).toFixed(1)}%`} />,
            }}
            onClick={() => {
              navigate(`/topic/view/SelectTopicSituation/student`);
            }}
            chart={<ReactECharts option={unselectedChartOption} style={{ height: '500px', width: '100%' }} />}
          />
        </StatisticCard.Group>
      </RcResizeObserver>
    </div>
  );
};

export default YourComponent;

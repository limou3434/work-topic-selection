import { PageContainer } from '@ant-design/pro-components';
import { useModel } from '@umijs/max';
import { Card, Divider, theme, Typography } from 'antd';
import React from 'react';

const { Title, Paragraph, Text } = Typography;

/**
 * 每个单独的卡片，为了复用样式抽成了组件
 * @param param0
 * @returns
 */
const InfoCard: React.FC<{
  title: string;
  index: number;
  desc: string;
  href: string;
}> = ({ title, href, index, desc }) => {
  const { useToken } = theme;

  const { token } = useToken();

  return (
    <div
      style={{
        backgroundColor: token.colorBgContainer,
        boxShadow: token.boxShadow,
        borderRadius: '8px',
        fontSize: '14px',
        color: token.colorTextSecondary,
        lineHeight: '22px',
        padding: '16px 19px',
        minWidth: '220px',
        flex: 1,
      }}
    >
      <div
        style={{
          display: 'flex',
          gap: '4px',
          alignItems: 'center',
        }}
      >
        <div
          style={{
            width: 48,
            height: 48,
            lineHeight: '22px',
            backgroundSize: '100%',
            textAlign: 'center',
            padding: '8px 16px 16px 12px',
            color: '#FFF',
            fontWeight: 'bold',
            backgroundImage:
              "url('https://gw.alipayobjects.com/zos/bmw-prod/daaf8d50-8e6d-4251-905d-676a24ddfa12.svg')",
          }}
        >
          {index}
        </div>
        <div
          style={{
            fontSize: '16px',
            color: token.colorText,
            paddingBottom: 8,
          }}
        >
          {title}
        </div>
      </div>
      <div
        style={{
          fontSize: '14px',
          color: token.colorTextSecondary,
          textAlign: 'justify',
          lineHeight: '22px',
          marginBottom: 8,
        }}
      >
        {desc}
      </div>
      <a href={href} target="_blank" rel="noreferrer">
        了解更多 {'>'}
      </a>
    </div>
  );
};

const Welcome: React.FC = () => {
  const { token } = theme.useToken();
  const { initialState } = useModel('@@initialState');
  return (
    <PageContainer>
      <Card
        style={{
          borderRadius: 8,
        }}
        bodyStyle={{
          backgroundImage:
            //@ts-ignore
            initialState?.settings?.navTheme === 'realDark'
              ? 'background-image: linear-gradient(75deg, #1A1B1F 0%, #191C1F 100%)'
              : 'background-image: linear-gradient(75deg, #FBFDFF 0%, #F5F7FF 100%)',
        }}
      >
        <Title style={{ textAlign: 'center' }}>欢迎使用毕业设计选题系统🎉</Title>
        <div
          style={{
            backgroundPosition: '100% -30%',
            backgroundRepeat: 'no-repeat',
            backgroundSize: '274px auto',
          }}
        >
          <p
            style={{
              fontSize: '14px',
              color: token.colorTextSecondary,
              lineHeight: '22px',
              marginTop: 16,
              marginBottom: 32,
              width: '65%',
            }}
          ></p>
          <div
            style={{
              display: 'flex',
              flexWrap: 'wrap',
              gap: 16,
            }}
          >
            <InfoCard
              index={1}
              href="https://www.nfu.edu.cn/"
              title="了解 广州南方学院学校"
              desc="广州南方学院（原中山大学南方学院）是 2006 年经教育部批准设立的综合性应用型普通本科高等学校，一直致力于建设国内特色..."
            />
            <InfoCard
              index={2}
              title="了解 电气与计算机工程学院"
              href="https://sece.nfu.edu.cn/"
              desc="电气与计算机工程学院，其前身为电子通信与软件工程系，始建于2006年，是广州南方学院（原中山大学南方学院）唯一的理工学院..."
            />
            <InfoCard
              index={3}
              title="了解 智能大数据工作室"
              href="https://limou3434.github.io/work-blog-website/"
              desc="本毕业设计选题系统由广州南方学院智能大数据平台工作室提供，是学校管理毕设选题的科学手段，为学校提高教学质量提供重要支持..."
            />
          </div>
        </div>
        <Divider />
        <Typography>
          <Title level={2}>1.如何使用?</Title>
          <Title level={3}>1.1.学生使用手册</Title>
          <Paragraph>作为学生，您的主要任务就是</Paragraph>
          <Paragraph>
            <Text strong>
              <ul>
                <li>
                  <Text code>确认预选毕业设计题目</Text> 或 <Text code>取消预选毕业设计题目</Text>
                </li>
                <li>
                  <Text code>确认提交毕业设计题目</Text> 或 <Text code>取消提交毕业设计题目</Text>
                </li>
              </ul>
            </Text>
          </Paragraph>
          <Paragraph>
            学生应当在题目开放时间前，预选自己心仪的{' '}
            <Text code>毕业设计题目（后续简称“题目”）</Text>
            ，然后在题目开放时间内抢夺题目，每一个学生最终只能确认选择一个题目，并且在选择题目瞬间自动清空所有的预选清单，并且无法再次预选题目。
          </Paragraph>
          <Paragraph>
            本系统 <Text mark>虽然提供取消当前已选题目的功能，但只根据学校的安排进行开放</Text>
            。若无法取消当前已选题目，则需要联系自己当前已选题目的对应导师。
          </Paragraph>
          <Paragraph>
            请不要过分使用某些连点器工具来使用本网站，
            <Text style={{ color: 'red' }}>一旦被系统检测到账号异常行为将会进行临时封号</Text>
            ，将严重导致您的选题安排。
          </Paragraph>
          图片演示...
          <Title level={3}>1.2.教师使用手册</Title>
          <Paragraph>作为教师，您的主要任务就是</Paragraph>
          <Paragraph>
            <Text strong>
              <ul>
                <li>
                  <Text code>发布题目</Text> 或 <Text code>修改题目</Text>
                </li>
                <li>
                  <Text code>查看题目状态</Text>
                </li>
                <li>
                  <Text code>可以根据情况为已经审核通过的题目选择学生</Text>
                </li>
              </ul>
            </Text>
          </Paragraph>
          图片演示...
          <Title level={3}>1.3.主任使用手册</Title>
          <Paragraph>作为主任，您的主要任务就是</Paragraph>
          <Paragraph>
            <Text strong>
              <ul>
                <li>
                  <Text code>审核题目（通过 或 打回）</Text>
                </li>
                <li>
                  <Text code>查看本系部学生的选题情况</Text>
                </li>
                <li>
                  <Text code>快速导出选题情况表格文件</Text>
                </li>
              </ul>
            </Text>
          </Paragraph>
          图片演示...
          <Title level={2}>2.遇到问题?</Title>
          <Paragraph>
            如果您在使用的过程中遇到问题，先阅读下面常见的解决方案，尝试是否能够解决，如果实在遇到解决不了的问题可以发送邮箱到{' '}
            <Text code>898738804@qq.com</Text>
          </Paragraph>
          <Paragraph>
            <ul>
              <li>忘记密码、没有账号、无故封号？</li>
            </ul>
          </Paragraph>
        </Typography>
      </Card>
    </PageContainer>
  );
};

export default Welcome;

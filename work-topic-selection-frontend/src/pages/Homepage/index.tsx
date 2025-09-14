import { PageContainer } from '@ant-design/pro-components';
import { useModel } from '@umijs/max';
import { Card, Collapse, Divider, Image, theme, Typography } from 'antd';
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
  const blockContent =
`邮箱格式：
标题：毕设系统+真实姓名+学号/工号
内容：电话号码+具体问题
`;

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
            学生应当<Text underline>在题目开放前</Text>，预选自己心仪的 <Text code>毕业设计题目（后续简称“题目”）</Text>。而<Text underline>在题目开放后</Text>可以抢夺题目，每一个学生最终只能确认选择一个题目，并且在选择题目瞬间自动清空所有的预选清单，无法再次预选题目（除非取消已经提交的题目）。
          </Paragraph>
          <Paragraph>
            本系统 <Text mark>虽然提供取消当前已经提交题目的功能，但只根据学校的安排进行开放</Text>。若您发现无法取消当前已提交的题目，则需要联系自己当前已提交题目所对应导师进行处理。
          </Paragraph>
          <Paragraph>
            抢题过程中，请不要过分使用某些连点器工具来使用本系统，<Text style={{ color: 'red' }}>一旦被系统检测到账号异常行为将会进行临时封号</Text>，严重将导致您的选题安排。
          </Paragraph>
          <Paragraph>
            <Collapse
              size="small"
              items={[{ key: '1', label: '学生端简易演示过程', children:
                  <>
                    <Paragraph>
                      <Image
                        width={"100%"}
                        src={"./steps/student/1.jpg"}
                      />
                      <div style={{ textAlign: 'center', marginTop: 8, color: '#666' }}>
                        （1）学生在题目开放前，点击菜单栏“学生选题 → 预选选题”，可以查看不同教师所发布的题目，以及不同题目的详细情况
                      </div>
                    </Paragraph>
                    <Paragraph>
                      <Image
                        width={"100%"}
                        src={"./steps/student/2.jpg"}
                      />
                      <div style={{ textAlign: 'center', marginTop: 8, color: '#666' }}>
                        （2）虽然题目暂时还没有开放，但是可以根据学生的个人喜好，点击“预选题目”进行预选，预选后的多个题目将会出现在“提交选题”中的列表中，如果题目不满意可以取消预选
                      </div>
                    </Paragraph>
                    <Paragraph>
                      <Image
                        width={"100%"}
                        src={"./steps/student/3.jpg"}
                      />
                      <div style={{ textAlign: 'center', marginTop: 8, color: '#666' }}>
                        （3）开放选题开放后，点击菜单栏“学生选题 → 提交选题”，在多个提前预选的题目中，学生点击“确认选题”后最终只能确认一个题目
                      </div>
                    </Paragraph>
                    <Paragraph>
                      <Image
                        width={"100%"}
                        src={"./steps/student/4.jpg"}
                      />
                      <div style={{ textAlign: 'center', marginTop: 8, color: '#666' }}>
                        （4）点击菜单栏“学生选题 → 查看选题”后就可以查看最终选得题目的详细信息
                      </div>
                    </Paragraph>
                  </>
              }]}
            />
          </Paragraph>

          <Title level={3}>1.2.教师使用手册</Title>
          <Paragraph>作为教师，您的主要任务就是</Paragraph>
          <Paragraph>
            <Text strong>
              <ul>
                <li>
                  <Text code>发布题目 或 修改题目</Text>
                </li>
                <li>
                  <Text code>查看题目状态</Text>
                </li>
                <li>
                  <Text code>可以根据情况为已经审核通过的题目选择学生（双选）</Text>
                </li>
              </ul>
            </Text>
          </Paragraph>
          <Paragraph>
            <Collapse
              size="small"
              items={[{ key: '1', label: '教师端简易演示过程', children:
              <>
                <Paragraph>
                  <Image
                    width={"100%"}
                    src={"./steps/teacher/1.jpg"}
                  />
                  <div style={{ textAlign: 'center', marginTop: 8, color: '#666' }}>
                    （1）点击菜单栏“教师发布 → 发布题目和修改题目”，这里可以看到自己发布的所有题目
                  </div>
                </Paragraph>
                <Paragraph>
                  <Image
                    width={"100%"}
                    src={"./steps/teacher/2.jpg"}
                  />
                  <div style={{ textAlign: 'center', marginTop: 8, color: '#666' }}>
                    （2）点击表格上方的“添加题目”，填写关于题目的信息表单
                  </div>
                </Paragraph>
                <Paragraph>
                  <Image
                    width={"100%"}
                    src={"./steps/teacher/3.jpg"}
                  />
                  <div style={{ textAlign: 'center', marginTop: 8, color: '#666' }}>
                    （3）点击“提交”按钮后即可发布题目，等待主任审核题目通过
                  </div>
                </Paragraph>
                <Paragraph>
                  <Image
                    width={"100%"}
                    src={"./steps/teacher/4.jpg"}
                  />
                  <div style={{ textAlign: 'center', marginTop: 8, color: '#666' }}>
                    （4）点击表格列“操作”区域的“编辑”后，进行重新编辑后点击“保存”，就可以修改已经提交的选题（您也可以直接点击“删除”后重新添加）
                  </div>
                </Paragraph>
                <Paragraph>
                  <Image
                    width={"100%"}
                    src={"./steps/teacher/5.jpg"}
                  />
                  <div style={{ textAlign: 'center', marginTop: 8, color: '#666' }}>
                    （5）若自己提交的题目状态显示为“打回”，则需要根据打回理由来重新修改题目信息，然后点击“操作”区域的“重新提交审核”使题目重新进入“待审核”的状态
                  </div>
                </Paragraph>
                <Paragraph>
                  <Image
                    width={"100%"}
                    src={"./steps/teacher/6.jpg"}
                  />
                  <div style={{ textAlign: 'center', marginTop: 8, color: '#666' }}>
                    （6）题目处于“已发布”状态后，教师可以寻找合适的学生进行双选，可以在“操作”区域点击“选择学生”
                  </div>
                </Paragraph>
                <Paragraph>
                  <Image
                    width={"100%"}
                    src={"./steps/teacher/7.jpg"}
                  />
                  <div style={{ textAlign: 'center', marginTop: 8, color: '#666' }}>
                    （7）您也可以点击“教师发布 → 查看选择自己的学生”查看选择自己题目的学生情况，也可以视情况点击“退选”帮助学生取消选题
                  </div>
                </Paragraph>
              </>
              }]}
            />
          </Paragraph>

          <Title level={3}>1.3.主任使用手册</Title>
          <Paragraph>作为主任，您的主要任务就是</Paragraph>
          <Paragraph>
            <Text strong>
              <ul>
                <li>
                  <Text code>审核题目（通过题目 或 打回题目）</Text>
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
          <Paragraph>
            <Collapse
              size="small"
              items={[{ key: '1', label: '主任端简易演示过程', children:
                  <>
                    <Paragraph>
                      <Image
                        width={"100%"}
                        src={"./steps/dept/1.jpg"}
                      />
                      <div style={{ textAlign: 'center', marginTop: 8, color: '#666' }}>
                        （1）点击菜单栏的“审核”，就可以查看本系教师的所有提交题目
                      </div>
                    </Paragraph>
                    <Paragraph>
                      <Image
                        width={"100%"}
                        src={"./steps/dept/2.jpg"}
                      />
                      <div style={{ textAlign: 'center', marginTop: 8, color: '#666' }}>
                        （2）审核题目过程中，若需要打回则需要填写“打回理由”
                      </div>
                    </Paragraph>
                    <Paragraph>
                      <Image
                        width={"100%"}
                        src={"./steps/dept/3.jpg"}
                      />
                      <div style={{ textAlign: 'center', marginTop: 8, color: '#666' }}>
                        （3）点击菜单栏的“选题 → 选题情况”，还可以查看本系内学生的选题情况，并且导出详细的选题情况
                      </div>
                    </Paragraph>
                  </>
              }]}
            />
          </Paragraph>

          <Title level={2}>2.遇到问题?</Title>
          <Paragraph>
            如果您在使用的过程中遇到问题，先尝试自己是否能够解决，如果实在遇到解决不了的问题可以发送邮箱到 <Text code>898738804@qq.com</Text> 中，邮件格式如下：
          </Paragraph>
          <Paragraph>
            <pre>{blockContent}</pre>
          </Paragraph>
        </Typography>
      </Card>
    </PageContainer>
  );
};

export default Welcome;

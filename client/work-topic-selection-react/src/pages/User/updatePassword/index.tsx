import { Footer } from '@/components';
import { LockOutlined, UserOutlined } from '@ant-design/icons';
import { LoginForm, ProFormText } from '@ant-design/pro-components';
import { useIntl } from '@ant-design/pro-provider';
import { Helmet, Link, history } from '@umijs/max';
import { Tabs, message } from 'antd';
import { createStyles } from 'antd-style';
import React, { useState } from 'react';
import Settings from '../../../../config/defaultSettings';
import {userUpdatePasswordUsingPost} from "@/services/work-topic-selection/userController";


const useStyles = createStyles(({ token }) => {
  return {
    action: {
      marginLeft: '8px',
      color: 'rgba(0, 0, 0, 0.2)',
      fontSize: '24px',
      verticalAlign: 'middle',
      cursor: 'pointer',
      transition: 'color 0.3s',
      '&:hover': {
        color: token.colorPrimaryActive,
      },
    },
    lang: {
      width: 42,
      height: 42,
      lineHeight: '42px',
      position: 'fixed',
      right: 16,
      borderRadius: token.borderRadius,
      ':hover': {
        backgroundColor: token.colorBgTextHover,
      },
    },
    container: {
      display: 'flex',
      flexDirection: 'column',
      height: '100vh',
      overflow: 'auto',
      backgroundImage:
        "url('https://mdn.alipayobjects.com/yuyan_qk0oxh/afts/img/V-_oS6r-i7wAAAAAAAAAAAAAFl94AQBr')",
      backgroundSize: '100% 100%',
    },
  };
});

const Register: React.FC = () => {
  const [type, setType] = useState<string>('account');
  const { styles } = useStyles();
  useIntl();
  const handleSubmit = async (values: API.UserUpdatePassword) => {
    // @ts-ignore
    const { updatePassword2, updatePassword } = values;
    // 校验密码确认字段
    if (updatePassword2 !== updatePassword) {
      message.error('两次输入的密码不一致');
      return;
    }
    try {
      // 注册
      const res = await userUpdatePasswordUsingPost(values);

      if (res.code===0) {

        message.success(res.message);
        // 重定向到登录页面
        history.push('/user/login');
        return;
      } else {
       message.error(res.message)
      }
    } catch (error) {
      // 处理注册异常
      message.error('修改失败，请稍后重试');
    }
  };
  return (
    <div className={styles.container}>
      <Helmet>
        <title>
          {'修改密码'}- {Settings.title}
        </title>
      </Helmet>
      <div
        style={{
          flex: '1',
          padding: '32px 0',
        }}
      >
        <LoginForm
          contentStyle={{
            minWidth: 280,
            maxWidth: '75vw',
          }}
          submitter={{
            searchConfig: {
              submitText: '修改密码',
            },
          }}
          logo={<img alt="logo" src="/logo_256.png" />}
          title="毕业设计选题系统"
          subTitle={'智能大数据工作室'}
          onFinish={async (values) => {
            await handleSubmit(values as API.UserUpdatePassword);
          }}
        >
          <Tabs
            activeKey={type}
            onChange={setType}
            centered
            items={[
              {
                key: 'account',
                label: '修改密码',
              },
            ]}
          />
          {type === 'account' && (
            <>
              <ProFormText
                name="userAccount"
                fieldProps={{
                  size: 'large',
                  //@ts-ignore
                  prefix: <UserOutlined />,
                }}
                placeholder={'请输入账户'}
                rules={[
                  {
                    required: true,
                    message: '用户名是必填项！',
                  },
                ]}
              />
              <ProFormText.Password
                name="userPassword"
                fieldProps={{
                  size: 'large',
                  //@ts-ignore
                  prefix: <LockOutlined />,
                }}
                placeholder={'请输入密码'}
                rules={[
                  {
                    required: true,
                    min: 8,
                    message: '密码不少于8位',
                  },
                ]}
              />
              <ProFormText.Password
                name="updatePassword"
                fieldProps={{
                  size: 'large',
                  //@ts-ignore
                  prefix: <LockOutlined />,
                }}
                placeholder={'请输入修改密码'}
                rules={[
                  {
                    required: true,
                    min: 8,
                    message: '密码不少于8位',
                  },
                ]}
              />
              <ProFormText.Password
                name="updatePassword2"
                fieldProps={{
                  size: 'large',
                  //@ts-ignore
                  prefix: <LockOutlined />,
                }}
                placeholder={'请再次输入修改密码'}
                rules={[
                  {
                    required: true,
                    min: 8,
                    message: '密码不少于8位',
                  },
                ]}
              />
            </>
          )}
          <div
            style={{
              marginBottom: 60,
            }}
          >
            <div>
              <Link
                style={{
                  float: 'right',
                }}
                to="/user/login"
              >
                返回登录页面
              </Link>
            </div>
          </div>
        </LoginForm>
      </div>
      <Footer />
    </div>
  );
};
export default Register;

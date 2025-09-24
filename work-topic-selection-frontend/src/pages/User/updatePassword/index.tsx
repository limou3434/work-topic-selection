import {Footer} from '@/components';
import {InfoCircleOutlined, LockOutlined, UserOutlined} from '@ant-design/icons';
import {LoginForm, ProFormText} from '@ant-design/pro-components';
import {useIntl} from '@ant-design/pro-provider';
import {Helmet, history, Link} from '@umijs/max';
import {message, Tabs, Tooltip} from 'antd';
import {createStyles} from 'antd-style';
import React, {useRef, useState} from 'react';
import Settings from '../../../../config/defaultSettings';
import {sendCodeUsingPost, userUpdatePasswordUsingPost} from "@/services/work-topic-selection/userController";

// eslint-disable-next-line @typescript-eslint/no-unused-vars
const useStyles = createStyles(({token}) => ({
  container: {
    display: 'flex',
    flexDirection: 'column',
    height: '100vh',
    overflow: 'auto',
    backgroundImage:
      "url('https://mdn.alipayobjects.com/yuyan_qk0oxh/afts/img/V-_oS6r-i7wAAAAAAAAAAAAAFl94AQBr')",
    backgroundSize: '100% 100%',
  },
}));

const Register: React.FC = () => {
  const {styles} = useStyles();
  useIntl();

  const [type, setType] = useState<string>('account');
  const formRef = useRef<any>();
  const [countdown, setCountdown] = useState<number>(0);
  const [useTempPassword, setUseTempPassword] = useState<boolean>(false);

  // 发送临时密码
  const handleSendCode = async () => {
    const userAccount = formRef.current?.getFieldValue('userAccount');
    if (!userAccount) {
      message.error('请先输入账号');
      return;
    }

    try {
      const res = await sendCodeUsingPost({userAccount});
      if (res.code === 0) {
        message.success('临时密码已发送，请在下方输入');
        setCountdown(60);
        setUseTempPassword(true); // 激活临时密码模式
        const timer = setInterval(() => {
          setCountdown(prev => {
            if (prev <= 1) {
              clearInterval(timer);
              return 0;
            }
            return prev - 1;
          });
        }, 1000);
      } else {
        message.error(res.message);
      }
    } catch {
      message.error('发送失败，请稍后重试');
    }
  };

  // 提交修改密码
  const handleSubmit = async (values: any) => {
    const {updatePassword, updatePassword2, userPassword, tempPasswordInput, userAccount, email} = values;


    if (updatePassword !== updatePassword2) {
      message.error('两次输入的密码不一致');
      return;
    }

    const payload: any = {userAccount, updatePassword, email}; // <-- 加上 email

    if (useTempPassword) {
      if (!tempPasswordInput) {
        message.error('请输入临时密码');
        return;
      }
      payload.code = tempPasswordInput; // 临时密码提交到 code 字段
    } else {
      if (!userPassword) {
        message.error('请输入原密码');
        return;
      }
      payload.userPassword = userPassword; // 旧密码提交到 userPassword
    }

    try {
      const res = await userUpdatePasswordUsingPost(payload);
      if (res.code === 0) {
        message.success(res.message);
        history.push('/user/login');
      } else {
        message.error(res.message);
      }
    } catch (error) {
      message.error('修改失败，请稍后重试');
    }
  };

  return (
    <div className={styles.container}>
      <Helmet>
        <title>{'修改密码'}- {Settings.title}</title>
      </Helmet>
      <div style={{flex: 1, padding: '32px 0'}}>
        <LoginForm
          formRef={formRef}
          contentStyle={{minWidth: 280, maxWidth: '75vw'}}
          submitter={{searchConfig: {submitText: '修改密码'}}}
          logo={<img alt="logo" src="/logo_256.png"/>}
          title="毕设选题系统"
          subTitle="智能大数据工作室"
          onFinish={async values => await handleSubmit(values)}
        >
          <Tabs
            activeKey={type}
            onChange={setType}
            centered
            items={[{key: 'account', label: '修改密码'}]}
          />
          {type === 'account' && (
            <>
              <ProFormText
                name="userAccount"
                fieldProps={{size: 'large', prefix: <UserOutlined/>}}
                placeholder="请输入账户"
                rules={[{required: true, message: '账号必填！'}]}
              />

              {!useTempPassword && (
                <ProFormText.Password
                  name="userPassword"
                  fieldProps={{size: 'large', prefix: <LockOutlined/>}}
                  placeholder="请输入原密码"
                  rules={[{required: true, min: 8, message: '原密码不少于8位'}]}
                />
              )}

            {useTempPassword && (
              <ProFormText.Password
                name="tempPasswordInput"
                fieldProps={{size: 'large', prefix: <LockOutlined/>}}
                placeholder="请输入临时密码"
                rules={[{required: true, message: '临时密码必填'}]}
              />
            )}

              <ProFormText.Password
                name="updatePassword"
                fieldProps={{size: 'large', prefix: <LockOutlined/>}}
                placeholder="请输入新密码"
                rules={[{required: true, min: 8, message: '新密码不少于8位'}]}
              />
              <ProFormText.Password
                name="updatePassword2"
                fieldProps={{size: 'large', prefix: <LockOutlined/>}}
                placeholder="请再次输入新密码"
                rules={[{required: true, min: 8, message: '新密码不少于8位'}]}
              />

              <ProFormText
                name="email"
                fieldProps={{
                  size: 'large', prefix: <UserOutlined/>, suffix: (
                    <Tooltip
                      title="如果不填写，忘记密码时无法通过邮箱获取临时密码，或者邮箱填写错误无法解绑，需要联系管理员 898738804@qq.com 进行修改。"
                      placement="right"
                    >
                      <InfoCircleOutlined style={{color: 'rgba(0,0,0,.45)'}}/>
                    </Tooltip>
                  ),
                }}
                placeholder="请输入 QQ 邮箱（选填）"
                rules={[
                  {required: false, message: '邮箱选填！'},
                  {type: 'email', message: '请输入正确的邮箱格式'}
                ]}
              />

            </>
          )}

          <div style={{marginBottom: 60}}>
            <div style={{marginBottom: 60, display: 'flex', justifyContent: 'space-between'}}>
              <a
                style={{
                  cursor: countdown > 0 ? 'not-allowed' : 'pointer',
                  color: countdown > 0 ? '#999' : '#1890ff'
                }}
                onClick={() => countdown === 0 && handleSendCode()}
              >
                {countdown > 0 ? `重新获取(${countdown}s)` : '忘记密码？获取临时密码！'}
              </a>
              <Link to="/user/login" style={{float: 'right'}}>
                返回登录页面
              </Link>
            </div>
          </div>
        </LoginForm>
      </div>
      <Footer/>
    </div>
  );
};

export default Register;

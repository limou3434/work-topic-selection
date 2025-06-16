import { GithubOutlined } from '@ant-design/icons';
import { DefaultFooter } from '@ant-design/pro-components';
import React from 'react';
import { Divider } from 'antd';

const Footer: React.FC = () => {
  const filingNumber = '粤ICP备2025406450号-1';
  const defaultMessage = '智能大数据工作室出品';
  const currentYear = new Date().getFullYear();
  return (
    <>
      <Divider />
      <DefaultFooter
        style={{
          background: 'none',
        }}
        copyright={`${currentYear} ${filingNumber} ${defaultMessage}`}
        links={[
          {
            key: 'github',
            title: <GithubOutlined />,
            href: '',
            blankTarget: true,
          },
          {
            key: '毕业设计选题系统',
            title: '毕业设计选题系统',
            href: 'https://wts.edtechhub.com.cn/',
            blankTarget: true,
          },
          {
            key: '前往 QQ 邮箱联系管理员以解决问题（管理员邮箱为 898738804@qq.com）',
            title: '前往 QQ 邮箱联系管理员',
            href: 'https://mail.qq.com/',
            blankTarget: true,
          },
        ]}
      />
    </>
  );
};

export default Footer;

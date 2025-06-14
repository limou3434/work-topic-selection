import { GithubOutlined } from '@ant-design/icons';
import { DefaultFooter } from '@ant-design/pro-components';
import React from 'react';

const Footer: React.FC = () => {
  const defaultMessage = '智能大数据工作室出品';
  const currentYear = new Date().getFullYear();
  return (
    <DefaultFooter
      style={{
        background: 'none',
      }}
      copyright={`${currentYear} ${defaultMessage}`}
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
      ]}
    />
  );
};

export default Footer;

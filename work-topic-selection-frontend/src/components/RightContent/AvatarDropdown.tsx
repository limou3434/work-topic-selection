import {BankOutlined, LogoutOutlined, SettingOutlined, TeamOutlined, UserOutlined} from '@ant-design/icons';
import {history, useModel} from '@umijs/max';
import {message, Space, Spin, Typography} from 'antd';
import {createStyles} from 'antd-style';
import {stringify} from 'querystring';
import type {MenuInfo} from 'rc-menu/lib/interface';
import React, {useCallback} from 'react';
import {flushSync} from 'react-dom';
import HeaderDropdown from '../HeaderDropdown';
import {USER_ROLE_MAP} from '@/constants/user';
import {userLogoutUsingPost} from '@/services/work-topic-selection/userController';

export type GlobalHeaderRightProps = {
  menu?: boolean;
  children?: React.ReactNode;
};


const {Text} = Typography;

export const AvatarName = () => {
  const {initialState} = useModel('@@initialState');
  const {currentUser} = initialState || {};
  const displayText = currentUser
    ? `${USER_ROLE_MAP[currentUser.userRole as 0 | 1 | 2 | 3]} - ${currentUser.userName}`
    : '';

  // 构建详细的tooltip内容
  return (
    <span
      style={{
        display: 'inline-block',
        maxWidth: 120, // 可以根据布局调整
        overflow: 'hidden',
        textOverflow: 'ellipsis',
        whiteSpace: 'nowrap',
        verticalAlign: 'middle',
      }}
    >
        {displayText}
      </span>
  );
};

const useStyles = createStyles(({token}) => {
  return {
    action: {
      display: 'flex',
      height: '48px',
      marginLeft: 'auto',
      overflow: 'hidden',
      alignItems: 'center',
      padding: '0 8px',
      cursor: 'pointer',
      borderRadius: token.borderRadius,
      '&:hover': {
        backgroundColor: token.colorBgTextHover,
      },
    },
  };
});

export const AvatarDropdown: React.FC<GlobalHeaderRightProps> = ({menu, children}) => {
  /**
   * 退出登录，并且将当前的 url 保存
   */
  const loginOut = async () => {
    const {search, pathname} = window.location;
    const urlParams = new URL(window.location.href).searchParams;
    /** 此方法会跳转到 redirect 参数所在的位置 */
    const redirect = urlParams.get('redirect');
    // Note: There may be security issues, please note
    if (window.location.pathname !== '/user/login' && !redirect) {
      history.replace({
        pathname: '/user/login',
        search: stringify({
          redirect: pathname + search,
        }),
      });
    }
  };
  const {styles} = useStyles();

  const {initialState, setInitialState} = useModel('@@initialState');

  const onMenuClick = useCallback(
    async (event: MenuInfo) => {
      const {key} = event;
      if (key === 'logout') {
        try {
          const res = await userLogoutUsingPost();
          message.success(res.message)
        } catch (error) {
          message.error('退出请求失败');
          return;
        }

        flushSync(() => {
          setInitialState((s: any) => ({...s, currentUser: undefined}));
        });
        loginOut();
        return;
      }
      history.push(`/account/${key}`);
    },
    [setInitialState],
  );

  const loading = (
    <span className={styles.action}>
      <Spin
        size="small"
        style={{
          marginLeft: 8,
          marginRight: 8,
        }}
      />
    </span>
  );

  if (!initialState) {
    return loading;
  }

  const {currentUser} = initialState;

  if (!currentUser || !currentUser.userName) {
    return loading;
  }

  // 用户基本信息菜单项
  const userInfoItems = [
    {
      key: 'user-info',
      type: 'group' as const,
      label: (
        <Space direction="vertical" size={2} style={{width: '100%'}}>
          <Text strong style={{fontSize: '14px'}}>
            <UserOutlined style={{marginRight: 6}}/>
            {currentUser.userName}
          </Text>
          <Text type="secondary" style={{fontSize: '12px'}}>
            {USER_ROLE_MAP[currentUser.userRole as 0 | 1 | 2 | 3]}
          </Text>
          {currentUser.dept && (
            <Text type="secondary" style={{fontSize: '12px'}}>
              <BankOutlined style={{marginRight: 4}}/>
              {currentUser.dept}
            </Text>
          )}
          {currentUser.project && (
            <Text type="secondary" style={{fontSize: '12px'}}>
              <TeamOutlined style={{marginRight: 4}}/>
              {currentUser.project}
            </Text>
          )}
        </Space>
      ),
    },
    {
      type: 'divider' as const,
    },
  ];

  const menuItems = [
    ...userInfoItems,
    ...(menu
      ? [
        {
          key: 'center',
          icon: <UserOutlined/>,
          label: '个人中心',
        },
        {
          key: 'settings',
          icon: <SettingOutlined/>,
          label: '个人设置',
        },
        {
          type: 'divider' as const,
        },
      ]
      : []),
    {
      key: 'logout',
      icon: <LogoutOutlined/>,
      label: '退出登陆',
    },
  ];

  return (
    <HeaderDropdown
      menu={{
        selectedKeys: [],
        onClick: onMenuClick,
        items: menuItems,
      }}
      overlayStyle={{
        maxWidth: '280px',
        minWidth: '220px',
        padding: '8px 0'
      }}
    >
      {children}
    </HeaderDropdown>
  );
};

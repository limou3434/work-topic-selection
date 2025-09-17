import {Image, notification} from 'antd';
import React, {useEffect} from 'react';

const WarningNotification: React.FC = () => {
  useEffect(() => {
    if (window.location.pathname !== '/home') return;
    notification.warning({
      message: '本站警告',
      description: (
        <>
          严禁爬虫抓取、恶意刷取、压力攻击等网络非法行为，本站由腾讯云承当部分服务保护。违者一经发现，
          <span style={{ color: 'blue' }}>立刻封禁帐号</span>，上报学院处理，
          <span style={{ color: 'red' }}>严重事故将追究相应的法律责任</span>，还请自重自爱！
          <a href={'https://www.gov.cn/xinwen/2016-11/07/content_5129723.htm?utm_source=chatgpt.com'}>详情见《中华人民共和国网络安全法》相关法条。</a>
          <Image src="./gdsgat.png" style={{ width: '100%', borderRadius: 8 }} />
        </>
      ),
      placement: 'bottomRight', // 右下角
      duration: 10, // 10s 后自动关闭
    });
  }, []);

  return null;
};

export default WarningNotification;

package cn.com.edtechhub.worktopicselection.service.impl;

import cn.com.edtechhub.worktopicselection.manager.caffeine.CaffeineManager;
import cn.com.edtechhub.worktopicselection.service.SwitchService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 跨选题开关服务实现
 */
@Service
public class SwitchServiceImpl implements SwitchService {

    /**
     * 跨选题开关缓存 Key
     */
    private static final String CROSS_TOPIC_SWITCH = "cross-topic-switch";

    /**
     * 注入 Caffeine 管理器
     */
    @Resource
    private CaffeineManager caffeineManager;

    /**
     * 所有读写操作共用一把锁, 保证管理员的设置会立刻影响所有学生是否允许跨选
     */
    private final ReentrantLock lock = new ReentrantLock();

    @Override
    public boolean isCrossTopicEnabled() {
        lock.lock();
        try {
            Object val = caffeineManager.get(CROSS_TOPIC_SWITCH);
            return val instanceof Boolean && (Boolean) val;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void setCrossTopicEnabled(boolean enabled) {
        lock.lock();
        try {
            caffeineManager.put(CROSS_TOPIC_SWITCH, enabled);
        } finally {
            lock.unlock();
        }
    }

}

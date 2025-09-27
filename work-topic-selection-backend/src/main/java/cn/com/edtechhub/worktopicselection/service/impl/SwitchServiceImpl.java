package cn.com.edtechhub.worktopicselection.service.impl;

import cn.com.edtechhub.worktopicselection.manager.caffeine.CaffeineManager;
import cn.com.edtechhub.worktopicselection.service.SwitchService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 开关服务实现
 */
@Service
public class SwitchServiceImpl implements SwitchService {

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
    public boolean isEnabled(String key) {
        lock.lock();
        try {
            Object val = caffeineManager.get(key);
            return val instanceof Boolean && (Boolean) val; // 如果是 Boolean 类型, 则返回其本身真值; 如果不是 Boolean 类型, 则直接返回 false
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void setEnabled(String key, boolean enabled) {
        lock.lock();
        try {
            caffeineManager.put(key, enabled);
        } finally {
            lock.unlock();
        }
    }

}

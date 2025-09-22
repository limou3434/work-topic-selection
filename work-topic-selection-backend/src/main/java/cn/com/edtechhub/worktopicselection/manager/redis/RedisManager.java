package cn.com.edtechhub.worktopicselection.manager.redis;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * Redis 管理类
 */
@Component
public class RedisManager {

    /**
     * 注入 RedisConfig 配置依赖
     */
    @Resource
    private RedisConfig redisConfig;

    /**
     * 注入 StringRedisTemplate 模板依赖
     */
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 设置值, 带过期时间(秒)
     *
     * @param key            键
     * @param value          值
     * @param timeoutSeconds 过期时间
     */
    public void setValue(String key, String value, long timeoutSeconds) {
        stringRedisTemplate.opsForValue().set(redisConfig.getKeyPrefix() + key, value, timeoutSeconds, TimeUnit.SECONDS);
    }

    /**
     * 设置值
     *
     * @param key   键
     * @param value 值
     */
    public void setValue(String key, String value) {
        stringRedisTemplate.opsForValue().set(redisConfig.getKeyPrefix() + key, value);
    }

    /**
     * 获取值
     *
     * @param key 键
     */
    public String getValue(String key) {
        return stringRedisTemplate.opsForValue().get(redisConfig.getKeyPrefix() + key);
    }

    /**
     * 删除键
     *
     * @param key 键
     */
    public void deleteKey(String key) {
        stringRedisTemplate.delete(redisConfig.getKeyPrefix() + key);
    }

    /**
     * List 操作
     *
     * @param key   键
     * @param value 值
     */
    public void rightPushList(String key, String value) {
        stringRedisTemplate.opsForList().rightPush(redisConfig.getKeyPrefix() + key, value);
    }

    /**
     * List 操作
     *
     * @param key 键
     */
    public String leftPopList(String key) {
        return stringRedisTemplate.opsForList().leftPop(redisConfig.getKeyPrefix() + key);
    }

    /**
     * Set 操作
     *
     * @param key    键
     * @param values 值
     */
    public void addSet(String key, String... values) {
        stringRedisTemplate.opsForSet().add(redisConfig.getKeyPrefix() + key, values);
    }

    /**
     * Set 操作
     *
     * @param key 键
     */
    public Object getSetMembers(String key) {
        return stringRedisTemplate.opsForSet().members(redisConfig.getKeyPrefix() + key);
    }

    /**
     * Hash 操作
     *
     * @param key   键
     * @param field 字段
     * @param value 值
     */
    public void putHash(String key, String field, String value) {
        stringRedisTemplate.opsForHash().put(redisConfig.getKeyPrefix() + key, field, value);
    }

    /**
     * Hash 操作
     *
     * @param key   键
     * @param field 字段
     */
    public Object getHash(String key, String field) {
        return stringRedisTemplate.opsForHash().get(redisConfig.getKeyPrefix() + key, field);
    }

}

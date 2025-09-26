package cn.com.edtechhub.worktopicselection.service;

/**
 * 跨选题开关服务接口
 */
public interface SwitchService {

    /**
     * 是否开启跨选题
     */
    boolean isCrossTopicEnabled();

    /**
     * 设置跨选题功能开关
     */
    void setCrossTopicEnabled(boolean enabled);

}

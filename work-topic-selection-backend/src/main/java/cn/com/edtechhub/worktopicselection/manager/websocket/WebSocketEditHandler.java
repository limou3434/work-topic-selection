package cn.com.edtechhub.worktopicselection.manager.websocket;

import cn.com.edtechhub.worktopicselection.exception.BusinessException;
import cn.com.edtechhub.worktopicselection.exception.CodeBindMessageEnums;
import cn.com.edtechhub.worktopicselection.model.entity.User;
import cn.com.edtechhub.worktopicselection.model.vo.UserVO;
import cn.com.edtechhub.worktopicselection.service.TopicService;
import cn.com.edtechhub.worktopicselection.service.UserService;
import cn.com.edtechhub.worktopicselection.utils.ThrowUtils;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 请求处理器
 */
@Component
@Slf4j
public class WebSocketEditHandler extends TextWebSocketHandler {

    /**
     * 注入用户服务
     */
    @Resource
    private UserService userService;

    /**
     * 注入题目服务
     */
    @Resource
    private TopicService topicService;

    /**
     * 保存所有连接的会话, 题目标识 -> 用户 ID, 用户会话集合
     */
    private final Map<Long, ConcurrentHashMap<Long, WebSocketSession>> sessions = new ConcurrentHashMap<>();

    /**
     * 链接建立后执行的方法
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // 获取属性
        Map<String, Object> attributes = session.getAttributes();
        User user = (User) attributes.get("user");
        Long topicId = (Long) attributes.get("topicId");

        // 添加集合
        if (topicId != null) {
            sessions.putIfAbsent(topicId, new ConcurrentHashMap<>());
            sessions.get(topicId).put(user.getId(), session);
            log.debug("用户 {} 连接到 topicId {} 的 WebSocket 链接", user.getUserName(), topicId);
        }
    }

    /**
     * 链接关闭后执行的方法
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, @NotNull CloseStatus status) {
        // 获取属性
        Map<String, Object> attributes = session.getAttributes();
        User user = (User) attributes.get("user");
        Long topicId = (Long) attributes.get("topicId");

        // 移除集合
        if (topicId != null && sessions.containsKey(topicId)) {
            ConcurrentHashMap<Long, WebSocketSession> sessions = this.sessions.get(topicId);
            sessions.remove(user.getId());
            if (sessions.isEmpty()) { // 如果集合为空, 直接删除集合
                this.sessions.remove(topicId);
            }
            log.debug("用户 {} 断开 topicId {} 的 WebSocket 连接", user.getUserName(), topicId);
        }
    }

    /**
     * 编写接收客户消息方法
     */
    @Override
    protected void handleTextMessage(@NotNull WebSocketSession webSocketSession, TextMessage textMessage) throws Exception {
        // 获取消息
        String payload = textMessage.getPayload();
        log.debug("收到 WebSocket 消息: {}", payload);

        try {
            // 获取会话属性
            Map<String, Object> attributes = webSocketSession.getAttributes();

            // 从当前 session 提取 topicId
            Long topicId = (Long) attributes.get("topicId");
            ThrowUtils.throwIf(topicId == null, CodeBindMessageEnums.PARAMS_ERROR, "链接上下文中的题目标识不存在");

            // 从当前 session 提取 userVO
            User user = (User) attributes.get("user");
            ThrowUtils.throwIf(user == null, CodeBindMessageEnums.PARAMS_ERROR, "链接上下文中的请求用户信息不存在");

            // 转化为结构对象
            ThrowUtils.throwIf(!JSONUtil.isJson(payload), CodeBindMessageEnums.PARAMS_ERROR, "用户消息中, 消息格式错误");
            WebSocketMessage webSocketMessage = JSONUtil.toBean(textMessage.getPayload(), WebSocketMessage.class);

            // 提取消息类型
            Integer typeCode = webSocketMessage.getTypeCode();
            ThrowUtils.throwIf(typeCode == null, CodeBindMessageEnums.PARAMS_ERROR, "用户消息中, 消息类型为空");
            assert typeCode != null;
            WebSocketMessageTypeEnum webSocketMessageTypeEnum = WebSocketMessageTypeEnum.getEnumByCode(typeCode);
            ThrowUtils.throwIf(webSocketMessageTypeEnum == null, CodeBindMessageEnums.PARAMS_ERROR, "用户消息中, 消息类型错误");
            assert webSocketMessageTypeEnum != null;

            // 提取消息内容
            String message = webSocketMessage.getMessage();
            ThrowUtils.throwIf(StringUtils.isBlank(message), CodeBindMessageEnums.PARAMS_ERROR, "用户消息中, 消息内容为空");

            // 广播发送消息
            ConcurrentHashMap<Long, WebSocketSession> sessions = this.sessions.get(topicId);
            if (sessions != null) {
                for (WebSocketSession s : sessions.values()) {
                    if (s.isOpen() && s != webSocketSession) { // 不给自己发
                        s.sendMessage(new TextMessage(payload));
                    }
                }
            }
        }
        catch (BusinessException e) {
            WebSocketMessage errorWebSocketMessage = new WebSocketMessage();
            errorWebSocketMessage.setTypeCode(WebSocketMessageTypeEnum.ERROR_MESSAGE.getCode());
            String errorMessage = JSONUtil.toJsonStr(e.getCodeBindMessageEnums() + e.getExceptionMessage());
            webSocketSession.sendMessage(new TextMessage(errorMessage));
        }
    }

}

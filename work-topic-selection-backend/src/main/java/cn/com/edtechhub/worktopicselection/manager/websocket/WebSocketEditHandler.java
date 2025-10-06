package cn.com.edtechhub.worktopicselection.manager.websocket;

import cn.com.edtechhub.worktopicselection.model.entity.StudentTopicSelection;
import cn.com.edtechhub.worktopicselection.model.entity.Topic;
import cn.com.edtechhub.worktopicselection.model.entity.User;
import cn.com.edtechhub.worktopicselection.model.enums.StudentTopicSelectionStatusEnum;
import cn.com.edtechhub.worktopicselection.model.vo.UserVO;
import cn.com.edtechhub.worktopicselection.service.TopicService;
import cn.com.edtechhub.worktopicselection.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
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
     * 保存所有连接的会话, topicId -> 用户会话集合
     */
    private final Map<Long, ConcurrentHashMap<String, WebSocketSession>> topicSessions = new ConcurrentHashMap<>();

    /**
     * 链接建立后执行的方法
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 获取属性
        Map<String, Object> attributes = session.getAttributes();
        UserVO userVO = (UserVO) attributes.get("userVO");
        Long topicId = (Long) attributes.get("topicId");

        // 添加集合
        if (topicId != null) {
            topicSessions.putIfAbsent(topicId, new ConcurrentHashMap<>());
            topicSessions.get(topicId).put(userVO.getUserName(), session);
            log.debug("用户 {} 连接到 topicId {} 的 WebSocket 链接", userVO.getUserName(), topicId);
        }
    }

    /**
     * 链接关闭后执行的方法
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, @NotNull CloseStatus status) throws Exception {
        // 获取属性
        Map<String, Object> attributes = session.getAttributes();
        UserVO userVO = (UserVO) attributes.get("userVO");
        Long topicId = (Long) attributes.get("topicId");

        // 移除集合
        if (topicId != null && topicSessions.containsKey(topicId)) {
            ConcurrentHashMap<String, WebSocketSession> sessions = topicSessions.get(topicId);
            sessions.remove(userVO.getUserName());
            if (sessions.isEmpty()) {
                topicSessions.remove(topicId);
            }
            log.debug("用户 {} 断开 topicId {} 的 WebSocket 连接", userVO.getUserName(), topicId);
        }
    }

    /**
     * 编写接收客户消息方法
     */
    @Override
    protected void handleTextMessage(@NotNull WebSocketSession session, TextMessage message) throws Exception {
        // 获取消息
        String payload = message.getPayload();
        log.debug("收到 WebSocket 消息: {}", payload);

        // 获取会话属性
        Map<String, Object> attributes = session.getAttributes();

        // 从当前 session 提取 topicId
        Long topicId = (Long) attributes.get("topicId");
        UserVO userVO = (UserVO) attributes.get("userVO");

        // 创建消息
        TopicStatusChangeMessage msg = new TopicStatusChangeMessage();
        msg.setType(WebSocketMessageTypeEnum.INFO.getCode()); // 可以是 INFO / ERROR / TOPIC_STATUS_CHANGE
        msg.setTopicId(topicId);
        msg.setStudentAccount(userVO.getUserName());
        msg.setMessage(payload);
        msg.setUserVO(userVO);

        // 消息序列化成 JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(msg);

        // 广播给同一 topic 下的所有用户
        ConcurrentHashMap<String, WebSocketSession> sessions = topicSessions.get(topicId);
        if (sessions != null) {
            for (WebSocketSession s : sessions.values()) {
                if (s.isOpen() && s != session) { // 不给自己发
                    s.sendMessage(new TextMessage(json));
                }
            }
        }
    }

}

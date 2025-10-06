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
     * 编写接收客户端消息的方法
     */
    @Override
    protected void handleTextMessage(@NotNull WebSocketSession session, TextMessage message) throws Exception {
        // 暂时不需要处理客户端发来的消息
        String payload = message.getPayload();
        log.debug("收到 WebSocket 消息: {}", payload);

        // 从当前 session 提取 topicId
        Map<String, Object> attributes = session.getAttributes();
        Long topicId = (Long) attributes.get("topicId");
        if (topicId == null) {
            log.warn("WebSocket 消息无 topicId，忽略");
            return;
        }

        // 广播给同一 topic 下的所有用户
        ConcurrentHashMap<String, WebSocketSession> sessions = topicSessions.get(topicId);
        if (sessions != null) {
            for (WebSocketSession s : sessions.values()) {
                if (s.isOpen() && s != session) { // 不给自己发
                    s.sendMessage(new TextMessage(payload));
                }
            }
        }
    }

    /**
     * 向指定题目的教师发送通知
     * 
     * @param topicId 题目ID
     * @param studentAccount 学生账号
     * @param statusMessage 状态变更信息
     */
    public void notifyTeachersForTopicStatusChange(Long topicId, String studentAccount, String statusMessage) {
        try {
            // 获取题目信息
            Topic topic = topicService.getById(topicId);
            if (topic == null) {
                log.debug("未找到ID为 {} 的题目", topicId);
                return;
            }
            
            // 获取该题目的指导老师
            String teacherName = topic.getTeacherName();
            if (teacherName == null || teacherName.isEmpty()) {
                log.debug("题目 {} 没有指定指导老师", topicId);
                return;
            }
            
            // 获取当前登录学生信息
            User studentUser = userService.userGetCurrentLoginUser();
            if (studentUser == null) {
                log.debug("未找到当前登录的学生用户");
                return;
            }
            UserVO studentUserVO = userService.getUserVO(studentUser);
            
            // 构造通知消息
            TopicStatusChangeMessage changeMessage = new TopicStatusChangeMessage();
            changeMessage.setType(WebSocketMessageTypeEnum.TOPIC_STATUS_CHANGE.getCode());
            changeMessage.setTopicId(topicId);
            changeMessage.setStudentAccount(studentAccount);
            changeMessage.setMessage(statusMessage);
            changeMessage.setUserVO(studentUserVO);
            
            // 发送消息给该题目的指导老师
            if (topicSessions.containsKey(topicId)) {
                ConcurrentHashMap<String, WebSocketSession> sessions = topicSessions.get(topicId);
                WebSocketSession teacherSession = sessions.get(teacherName);
                if (teacherSession != null && teacherSession.isOpen()) {
                    // 创建 ObjectMapper
                    ObjectMapper objectMapper = new ObjectMapper();
                    // 配置序列化：将 Long 类型转为 String，解决丢失精度问题
                    SimpleModule module = new SimpleModule();
                    module.addSerializer(Long.class, ToStringSerializer.instance);
                    module.addSerializer(Long.TYPE, ToStringSerializer.instance); // 支持 long 基本类型
                    objectMapper.registerModule(module);
                    // 序列化为 JSON 字符串
                    String message = objectMapper.writeValueAsString(changeMessage);
                    TextMessage textMessage = new TextMessage(message);
                    teacherSession.sendMessage(textMessage);
                    log.debug("已向老师 {} 发送题目 {} 的状态变更通知: {}", teacherName, topicId, statusMessage);
                } else {
                    log.debug("老师 {} 未连接到topicId {} 的WebSocket", teacherName, topicId);
                }
            } else {
                log.debug("topicId {} 没有活跃的WebSocket连接", topicId);
            }
        } catch (Exception e) {
            log.error("发送题目状态变更通知失败", e);
        }
    }
    
    /**
     * 当学生选题状态发生变更时通知相关教师
     * 
     * @param studentTopicSelection 学生选题记录
     * @param oldStatus 旧状态
     * @param newStatus 新状态
     */
    public void notifyTeacherOnStatusChange(StudentTopicSelection studentTopicSelection, Integer oldStatus, Integer newStatus) {
        try {
            // 只有当状态确实发生变化时才发送通知
            if (oldStatus != null && oldStatus.equals(newStatus)) {
                return;
            }
            
            Long topicId = studentTopicSelection.getTopicId();
            String studentAccount = studentTopicSelection.getUserAccount();
            
            // 获取学生用户信息
            User studentUser = userService.userIsExist(studentAccount);
            if (studentUser == null) {
                log.debug("未找到学生用户: {}", studentAccount);
                return;
            }
            
            // 获取题目信息
            Topic topic = topicService.getById(topicId);
            if (topic == null) {
                log.debug("未找到ID为 {} 的题目", topicId);
                return;
            }
            
            // 构造状态变更信息
            String statusMessage = buildStatusChangeMessage(studentUser.getUserName(), 
                    oldStatus, newStatus, topic.getTopic());
            
            // 通过WebSocket通知教师
            notifyTeachersForTopicStatusChange(topicId, studentAccount, statusMessage);
        } catch (Exception e) {
            log.error("发送选题状态变更通知失败", e);
        }
    }
    
    /**
     * 构造状态变更信息
     * 
     * @param studentName 学生姓名
     * @param oldStatus 旧状态
     * @param newStatus 新状态
     * @param topicName 题目名称
     * @return 状态变更信息
     */
    private String buildStatusChangeMessage(String studentName, Integer oldStatus, Integer newStatus, String topicName) {
        StringBuilder message = new StringBuilder();
        message.append("学生 ").append(studentName).append(" 对题目《").append(topicName).append("》的选题状态发生变更：");
        
        String oldStatusDesc = getStatusDescription(oldStatus);
        String newStatusDesc = getStatusDescription(newStatus);
        
        if (oldStatus != null) {
            message.append("从 [").append(oldStatusDesc).append("] ");
        }
        message.append("变更为 [").append(newStatusDesc).append("]");
        
        return message.toString();
    }
    
    /**
     * 获取状态描述
     * 
     * @param statusCode 状态码
     * @return 状态描述
     */
    private String getStatusDescription(Integer statusCode) {
        if (statusCode == null) {
            return "未知状态";
        }
        
        StudentTopicSelectionStatusEnum statusEnum = StudentTopicSelectionStatusEnum.getEnums(statusCode);
        return statusEnum != null ? statusEnum.getDescription() : "未知状态(" + statusCode + ")";
    }
    
    /**
     * 发送学生选题状态变更通知
     * 
     * @param studentTopicSelection 学生选题记录
     * @param oldStatus 旧状态
     * @param newStatus 新状态
     */
    public void sendTopicStatusChangeNotification(StudentTopicSelection studentTopicSelection, Integer oldStatus, Integer newStatus) {
        try {
            notifyTeacherOnStatusChange(studentTopicSelection, oldStatus, newStatus);
        } catch (Exception e) {
            log.error("发送选题状态变更通知失败", e);
        }
    }
}

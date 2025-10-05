package cn.com.edtechhub.worktopicselection.manager.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ClassUtils;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * WebSocket 配置类
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Configuration
@EnableWebSocket // 启用 Spring WebSocket 支持
@Slf4j
public class WebSocketConfig implements WebSocketConfigurer {

    /**
     * 定义 WebSocket 服务端点（endpoint）的访问路径 Path
     */
    String path = "/ws/message";

    /**
     * 引入 WebSocket 拦截器依赖
     */
    @Resource
    private WsHandshakeInterceptor webSocketInterceptor;

    /**
     * 引入 WebSocket 管理者依赖
     */
    @Resource
    private EditHandler editHandler;

    /**
     * 注册 WebSocket 端点
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry
                .addHandler(editHandler, this.path) // 把自定义的消息处理器 editHandler 注册到 path 这个端点
                .addInterceptors(webSocketInterceptor) // 给这个 WebSocket 通信加一个握手拦截器, 用于连接前的校验或参数处理
                .setAllowedOrigins(this.getCorsRule()) // 允许所有来源跨域连接(生产环境中最好改成具体域名)
        ;
    }

    /**
     * 允许跨域规则
     */
    private String[] getCorsRule() {
        return Arrays.asList(
                "http://127.0.0.1:3000",
                "http://192.168.0.44:3000",
                "https://wts.edtechhub.com.cn"
        ).toArray(new String[0]);
    }

    /**
     * 打印配置
     */
    @PostConstruct
    public void printConfig() {
        Class<?> clazz = ClassUtils.getUserClass(this); // 获取原始类
        log.debug("[{}] path: {}", clazz.getSimpleName(), this.path);
        log.debug("[{}] getCorsRule: {}", clazz.getSimpleName(), this.getCorsRule());
    }


}

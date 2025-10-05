//package cn.com.edtechhub.worktopicselection.manager.websocket;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.socket.config.annotation.EnableWebSocket;
//import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
//import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
//
//import javax.annotation.PostConstruct;
//import javax.annotation.Resource;
//
///**
// * WebSocket 配置类
// *
// * @author <a href="https://github.com/limou3434">limou3434</a>
// */
//@Configuration
//@EnableWebSocket
//@Slf4j
//public class WebSocketConfig implements WebSocketConfigurer {
//
//    /**
//     * 引入 WebSocket 握手拦截器依赖
//     */
//    @Resource
//    private WsHandshakeInterceptor webSocketInterceptor;
//
//    /**
//     * 引入 WebSocket 图片编辑管理者依赖
//     */
//    @Resource
//    private EditHandler editHandler;
//
//    /**
//     *
//     * @param registry WebSocket 管理器
//     */
//    @Override
//    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
//        registry
//                .addHandler(editHandler, "/ws/picture/edit")
//                .addInterceptors(webSocketInterceptor)
//                .setAllowedOrigins("*");
//    }
//
//    /**
//     * 打印配置
//     */
//    @PostConstruct
//    public void printConfig() {
//        log.debug("[WebSocketConfig]");
//    }
//
//
//}

//package cn.com.edtechhub.worktopicselection.manager.websocket;
//
//import cn.com.edtechhub.workcollaborativeimages.manager.auth.SpaceUserAuthManager;
//import cn.com.edtechhub.workcollaborativeimages.service.PictureService;
//import cn.com.edtechhub.workcollaborativeimages.service.SpaceService;
//import cn.com.edtechhub.workcollaborativeimages.service.UserService;
//import lombok.extern.slf4j.Slf4j;
//import org.jetbrains.annotations.NotNull;
//import org.springframework.http.server.ServerHttpRequest;
//import org.springframework.http.server.ServerHttpResponse;
//import org.springframework.stereotype.Component;
//import org.springframework.web.socket.WebSocketHandler;
//import org.springframework.web.socket.server.HandshakeInterceptor;
//
//import javax.annotation.Resource;
//import java.util.Map;
//
///**
// * 拦截 WebSocket 的 HTTP 请求携带用户登录信息, 避免后续的请求无法获取用户登录信息
// */
//@Component
//@Slf4j
//public class WsHandshakeInterceptor implements HandshakeInterceptor {
//
//    /**
//     * 注入空间用户权限管理依赖
//     */
//    @Resource
//    private SpaceUserAuthManager spaceUserAuthManager;
//
//    /**
//     * 注入用户服务依赖
//     */
//    @Resource
//    private UserService userService;
//
//    /**
//     * 注入图片服务依赖
//     */
//    @Resource
//    private PictureService pictureService;
//
//    /**
//     * 注入空间服务依赖
//     */
//    @Resource
//    private SpaceService spaceService;
//
//    /**
//     * 在 WebSocket 握手开始前, 也就是 HTTP 升级开始前执行
//     *
//     * @param request    请求
//     * @param response   响应
//     * @param wsHandler  处理器
//     * @param attributes 属性
//     * @return 是否允许握手的布尔值
//     */
//    @Override
//    public boolean beforeHandshake(@NotNull ServerHttpRequest request, @NotNull ServerHttpResponse response, @NotNull WebSocketHandler wsHandler, @NotNull Map<String, Object> attributes) {
//        log.debug("WebSocket 握手开始前触发 beforeHandshake(), 其中 {} {} {}", request, response, wsHandler);
//        // 判断当前请求是不是一个基于 Servlet 的 HTTP 请求
////        if (request instanceof ServletServerHttpRequest) {
////            // 获取请求对象
////            HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
////
////            // 获取请求参数
////            Long userId = userService.userGetCurrentLonginUserId();
////            if (userId == null) {
////                log.error("用户尚未登录, 拒绝握手");
////                return false;
////            }
////
////            UserVO userVO = UserVO.removeSensitiveData(userService.userSearchById(userId));
////            if (userVO == null) {
////                log.error("用户不存在, 拒绝握手");
////                return false;
////            }
////
////            String pictureId = servletRequest.getParameter("pictureId");
////            if (StrUtil.isBlank(pictureId)) {
////                log.error("缺少图片参数, 拒绝握手"); // TODO: 尝试改为前端响应的模式
////                return false;
////            }
////
////            Picture picture = pictureService.getById(pictureId);
////            if (picture == null) {
////                log.error("图片不存在，拒绝握手");
////                return false;
////            }
////
////            Long spaceId = picture.getSpaceId();
////            Space space = null;
////            if (spaceId != null) {
////                space = spaceService.getById(spaceId);
////                if (space == null) {
////                    log.error("空间不存在，拒绝握手");
////                    return false;
////                }
////                if (space.getType() != SpaceTypeEnum.COLLABORATIVE.getCode()) {
////                    log.info("非团队空间，拒绝握手");
////                    return false;
////                }
////            }
////
////            List<String> permissionList = spaceUserAuthManager.getPermissionsByRoleEnum();
////            if (!permissionList.contains(SpaceUserPermissionConstant.PICTURE_EDIT)) {
////                log.error("没有图片编辑权限, 拒绝握手");
////                return false;
////            }
////            // 设置 attributes
////            attributes.put("userVO", userVO);
////            attributes.put("userId", userVO.getId());
////            attributes.put("pictureId", Long.valueOf(pictureId));
////        }
//        return true;
//    }
//
//    /**
//     * 在 WebSocket 握手结束后, 也就是 HTTP 升级结束后执行
//     *
//     * @param request   请求
//     * @param response  响应
//     * @param wsHandler 处理器
//     * @param exception 异常
//     */
//    @Override
//    public void afterHandshake(@NotNull ServerHttpRequest request, @NotNull ServerHttpResponse response, @NotNull WebSocketHandler wsHandler, Exception exception) {
//        log.debug("WebSocket 握手结束后触发 afterHandshake(), 其中 {} {} {}", request, response, wsHandler);
//    }
//
//}

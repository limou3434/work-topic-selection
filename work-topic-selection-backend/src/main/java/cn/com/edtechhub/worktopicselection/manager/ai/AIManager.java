package cn.com.edtechhub.worktopicselection.manager.ai;

import cn.com.edtechhub.worktopicselection.exception.CodeBindMessageEnums;
import cn.com.edtechhub.worktopicselection.utils.ThrowUtils;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI 管理类
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Component
@Slf4j
public class AIManager {

    @Resource
    private AIConfig aiConfig;

    /**
     * 发送 AI 请求并直接返回最终结果
     *
     * @param userId      用户标识
     * @param userContent 提问内容
     * @return AiResult
     */
    public AIResult sendAi(
            String userId,
            String userContent
    ) {
        // 请求 AI
        String response = this.sendAiRequest(userId, userId, userContent + aiConfig.getSystemRole());

        // 转化为包含 Map 事件的对象
        AIResponse aiResponse = this.mapToAiResponse(response);

        // 获取最终回复事件 Map
        Map<String, Object> secondReplyMap = aiResponse.getSecondReply();
        String payload = secondReplyMap.get("payload").toString();
        JSONObject payloadJson = JSONObject.parseObject(payload);
        String content = payloadJson.getString("content");
        System.out.println(content);

        // 设置正则表达式规则
        Pattern pattern = Pattern.compile("\\{[\\s\\S]*}");
        Matcher matcher = pattern.matcher(content);

        // 提取检验结果对象
        AIResult aiResult = null;
        if (matcher.find()) {
            String json = matcher.group();
            ObjectMapper mapper = new ObjectMapper();
            try {
                aiResult = mapper.readValue(json, AIResult.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            log.debug("最终结果: {}", aiResult);
        }
        return aiResult;
    }

    /**
     * 发送 AI 请求并直接返回回答内容字符串
     *
     * @param visitorBizId 用户 ID
     * @param sessionId    会话 ID
     * @param userContent  提问内容
     * @return String
     */
    private String sendAiRequest(
            String visitorBizId,
            String sessionId,
            String userContent
    ) {
        // 封装请求参数
        JSONObject request = new JSONObject();
        request.put("bot_app_key", aiConfig.getBotAppKey()); // 应用密钥
        request.put("visitor_biz_id", visitorBizId); // 用户 id
        request.put("session_id", sessionId); // 会话 id
        request.put("system_role", aiConfig.getSystemRole());
        request.put("content", userContent); // 用户提问内容
        request.put("streaming_throttle", 100); // 字符回包的积攒数量
        request.put("incremental", false); // 是否控制回复事件和思考事件中的 content 为增量内容 ( 可以避免成为 SSE 调用)
        request.put("visitor_labels", new JSONArray()); // 知识标签列表
        request.put("search_network", "disable"); // 是否打开联网搜索
        request.put("stream", "disable"); // 是否流式输出
        request.put("workflow_status", "disable"); // 关闭工作流模式
        log.debug("得到 request: {}", request.toJSONString());

        HttpResponse response = HttpRequest
                .post(aiConfig.getRequestUrl())
                .body(request.toJSONString())
                .execute();

        return response.body();
    }

    /**
     * 将接口返回的字符串映射为 AiResponse 对象
     *
     * @param responseBody 接口返回的原始字符串
     * @return AiResponse
     */
    private AIResponse mapToAiResponse(String responseBody) {
        // 正则表达式匹配 event 和对应的 data
        Pattern pattern = Pattern.compile("event:(.*?)\r?\ndata:(.*?)\r?\n", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(responseBody + "\n"); // 加个换行符确保最后一个匹配能被捕获

        // 计数器，用于区分同类型的 event
        int replyCount = 0;
        int tokenStatCount = 0;

        // 填充 AiResponse 对象
        AIResponse aiResponse = new AIResponse();
        while (matcher.find()) {
            String event = matcher.group(1).trim();
            String dataJson = matcher.group(2).trim();

            try {
                // 解析 data 为 JSONObject
                JSONObject dataObj = JSONObject.parseObject(dataJson);
                // 转换为 Map 用于非严格映射
                Map<String, Object> dataMap = new HashMap<>(dataObj);

                // 根据 event 类型和计数器分配到不同的属性
                if ("reply".equals(event)) {
                    replyCount++;
                    if (replyCount == 1) {
                        aiResponse.setFirstReply(dataMap);
                    } else if (replyCount == 2) {
                        aiResponse.setSecondReply(dataMap);
                    }
                } else if ("token_stat".equals(event)) {
                    tokenStatCount++;
                    if (tokenStatCount == 1) {
                        aiResponse.setFirstTokenStat(dataMap);
                    } else if (tokenStatCount == 2) {
                        aiResponse.setSecondTokenStat(dataMap);
                    }
                }
            } catch (Exception e) {
                ThrowUtils.throwIf(true, CodeBindMessageEnums.OPERATION_ERROR, "解析 AI 响应数据失败");
            }
        }

        return aiResponse;
    }


}

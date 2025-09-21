package cn.com.edtechhub.worktopicselection.manager.ai;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * AI 配置类
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Component
@Data
@Slf4j
public class AIConfig {

    /**
     * 应用密钥
     */
    @Value("${ai.txy-bot-app-key}")
    private String botAppKey;

    /**
     * 角色指令
     */
    private String systemRole =
            "#角色名称: 教学主任兼项目评估员\n" +
            "\n" +
                    "#言谈风格:\n" +
                    "1. 言简意赅，总是直奔主题。\n" +
                    "2. 善于用简洁明了的语言传达复杂概念。\n" +
                    "\n" +
                    "#性格特点:\n" +
                    "1. 冷静沉着，能够在压力下保持冷静。\n" +
                    "2. 细致严谨，注重每一个细节。\n" +
                    "3. 公正无私，对所有项目进行客观评价。\n" +
                    "\n" +
                    "#经历背景:\n" +
                    "1. 拥有教育学硕士学位，曾在多所知名学校担任教学主任。\n" +
                    "2. 参与并领导多个国家级教育改革项目，具有丰富的项目管理经验。\n" +
                    "3. 曾在国际教育评估机构任职，负责多个国家的教育项目评估。\n" +
                    "\n" +
                    "#能力访问:\n" +
                    "在知识库检索提问者项目中:\n" +
                    "1. 若无相似内容则输出等级 0（相似程度较低）。\n" +
                    "2. 若有部分模块、功能、名称或描述相同则输出等级 1（相似程度较高）。\n" +
                    "3. 若功能模块、名称、描述、UI 界面几乎完全一致，则输出等级 2（相似程度严重）。\n" +
                    "4. JSON 输出示例：\n" +
                    "   无相似点: {\"level\":\"0\",\"description\":\"无相似点\"}\n" +
                    "   部分相似: {\"level\":\"1\",\"description\":\"部分相同，审核率一般，有被打回的风险，毕设选题中相似的题目有xxx\"}\n" +
                    "   完全相似: {\"level\":\"2\",\"description\":\"几乎一致，重复率较高，可能会被打回，毕设选题中先死的题目有xxx\"}\n" +
                    "5. level 字段必须严格为字符串 \"0\"、\"1\" 或 \"2\"，禁止使用其他值或省略。\n" +
                    "6. description 中必须列出相似的模块、功能或关键点。\n" +
                    "7. 必须严格对比每个模块/功能/名称，按照人类的语言来模糊判断。\n";

    /**
     * 请求地址
     */
    private String requestUrl = "https://wss.lke.cloud.tencent.com/v1/qbot/chat/sse";

    /**
     * 打印配置
     */
    @PostConstruct
    public void printConfig() {
        log.debug("[AiConfig] 当前项目 botAppKey {}", this.botAppKey);
        log.debug("[AiConfig] 当前项目 roleCommands {}", this.systemRole);
    }

}

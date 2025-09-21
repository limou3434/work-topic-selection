package cn.com.edtechhub.worktopicselection.manager.sentine;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Redis 管理类
 */
@Component
@Slf4j
@Data
public class SentineManager {

    /**
     * 注入 SentineConfig 配置依赖
     */
    @Resource
    private SentineConfig sentineConfig;

    /**
     * 初始化限流规则
     *
     * @param entryName 资源名称
     * @param count     限流次数
     */
    public void initFlowRules(String entryName, Integer count) {
        // 添加限流规则
        List<FlowRule> rules = new ArrayList<>();
        FlowRule rule = new FlowRule();
        rule.setResource(entryName);
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setCount(count);
        rules.add(rule);
        FlowRuleManager.loadRules(rules);
    }

    /**
     * 初始化限流规则
     *
     * @param entryName 资源名称
     */
    public void initFlowRules(String entryName) {
        // 添加限流规则
        List<FlowRule> rules = new ArrayList<>();
        FlowRule rule = new FlowRule();
        rule.setResource(entryName);
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setCount(sentineConfig.getQps());
        rules.add(rule);
        FlowRuleManager.loadRules(rules);
    }

}

package cn.com.edtechhub.worktopicselection.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

/**
 * 全局跨域配置
 */
@Configuration
@Slf4j
public class CorsConfig implements WebMvcConfigurer {

    /**
     * 配置跨域共享
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry
                .addMapping("/**")
                .allowedOriginPatterns(this.getCorsRule().toArray(new String[0]))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 允许跨域规则
     */
    private List<String> getCorsRule() {
        return Arrays.asList(
                "http://127.0.0.1:3000",
                "http://10.10.174.232",
                "https://wts.edtechhub.com.cn"
        );
    }

    /**
     * 打印配置
     */
    @PostConstruct
    public void printConfig() {
        log.debug("[CorsConfig] 当前项目 Cors 跨域规则为 {}", this.getCorsRule());
    }

}

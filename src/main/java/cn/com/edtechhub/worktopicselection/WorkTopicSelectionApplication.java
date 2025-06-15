package cn.com.edtechhub.worktopicselection;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring Boot 启动类
 * 如果使用 IDEA 需要在启动前配置 .env 后才能启动
 * 如果使用 jdk 运行环境需要书写对应的环境变量才能启动
 * 如果使用 Docker 容器需要书写对应的环境变量才能启动
 *
 * @author <a href="https://github.com/limou3434">limou3434</a>
 */
@Slf4j
@SpringBootApplication()
@MapperScan("cn.com.edtechhub.worktopicselection.mapper")
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
public class WorkTopicSelectionApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorkTopicSelectionApplication.class, args);
        log.info("http://127.0.0.1:8000/doc.html");
    }

}

// TODO： 教师端查看自己发布的选题有问题(并且无法在未审核的时候看到...)
// TODO: 发布后的选题好像没有修改预选人数
// TODO: 用户提交表格后好像有些问题...因为多了空格会导致前端显示不出来...
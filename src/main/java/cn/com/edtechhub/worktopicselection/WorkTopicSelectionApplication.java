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

// TODO: 教师端可以看到审核状态没有问题, 但是有可以自己修改的错觉...应该取消状态编辑
// TODO: 管理端审核通过后无法修改新的审核时间...
// TODO: 一个系别的教师发布选题时, 只能选择和自己同个系别的系别, 系别主任也只能选这个主任
// TODO: 发布后的选题好像没有修改预选人数(?等待检查)
// TODO: 用户提交表格后好像有些问题...因为多了空格会导致前端显示不出来...
// TODO: 前端显示的登陆角色应该换一种形式展示, 不然下面太长的时候会显示奇怪
// TODO: 前端一些按钮需要优化一下, 向管理端看齐
// TODO: 思考...还是把权限加上去把...有错误再来修改

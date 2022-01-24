package cn.yiidii.sb;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 程序入口
 *
 * @author ed w
 * @since 1.0
 */
@Slf4j
@EnableScheduling
@ComponentScan("cn.yiidii")
@SpringBootApplication
public class SbApplication {

    public static void main(String[] args) {
        SpringApplication.run(SbApplication.class, args);
        log.info("系统启动成功");
    }
}

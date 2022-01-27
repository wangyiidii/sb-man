package cn.yiidii.sb.support;

import cn.yiidii.sb.service.RobotCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;


/**
 * 定时任务
 *
 * @author ed w
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SbScheduleApplicationRunner implements ApplicationRunner {

    private final RobotCacheService robotCacheService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info(">>>>>>>>>>>>>>>>>>>> 启动系统定时任务开始");
        robotCacheService.startTimerTask();
        log.info(">>>>>>>>>>>>>>>>>>>> 定时任系统务启动完成");
    }
}

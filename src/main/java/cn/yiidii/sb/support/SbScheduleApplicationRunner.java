package cn.yiidii.sb.support;

import cn.yiidii.sb.common.constant.SbScheduleNameConstant;
import cn.yiidii.sb.service.RobotCacheService;
import cn.yiidii.sb.service.SystemConfigService;
import cn.yiidii.sb.util.ScheduleTaskUtil;
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
    private final SystemConfigService systemConfigService;
    private final ScheduleTaskUtil scheduleTaskUtil;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info(">>>>>>>>>>>>>>>>>>>> 启动系统定时任务开始");
        robotCacheService.startTimerTask();
        scheduleTaskUtil.startCron(SbScheduleNameConstant.SYSTEM_CHECK_UPGRADE, () -> systemConfigService.timerCheckIfNeedUpdated(), "0 0/10 * * * ?");
        log.info(">>>>>>>>>>>>>>>>>>>> 定时任系统务启动完成");
    }
}

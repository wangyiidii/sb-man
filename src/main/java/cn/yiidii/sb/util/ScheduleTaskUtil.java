package cn.yiidii.sb.util;

import cn.hutool.core.util.StrUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronExpression;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

/**
 * 定时任务工具类
 *
 * @author ed w
 * @since 1.0
 */
@Slf4j
@Component
@DependsOn({"defaultTaskScheduler"})
@RequiredArgsConstructor
public class ScheduleTaskUtil {

    private Map<String, ScheduledFuture<?>> futureGroup = new HashMap<>(16);
    private Map<String, Runnable> taskGroup = new HashMap<>(16);

    public final ThreadPoolTaskScheduler defaultTaskScheduler;


    /**
     * 启动定时任务 （若任务名相同，则覆盖之前的任务）
     *
     * @param name 任务名称
     * @param task 任务
     * @param cron cron表达式
     */
    public void startCron(String name, Runnable task, String cron) {
        if (!CronExpression.isValidExpression(cron)) {
            String errMsg = StrUtil.format("启动定时任务[{}], cron表达式[{}]不合法", name, cron);
            log.error(errMsg);
            throw new RuntimeException(errMsg);
        }
        stopCron(name);
        ScheduledFuture<?> future = defaultTaskScheduler.schedule(task, new CronTrigger(cron));
        futureGroup.put(name, future);
        taskGroup.put(name, task);
        log.info(StrUtil.format("定时任务[{}({})]启动成功", name, cron));
    }


    /**
     * 停止任务
     *
     * @param name 任务名称
     */
    public void stopCron(String name) {
        ScheduledFuture<?> future = futureGroup.get(name);
        if (future != null) {
            future.cancel(true);
            log.info(StrUtil.format("定时任务[{}]停止成功", name));
        }
    }


    /**
     * 变更任务间隔，再次启动 先停止，在开启.
     *
     * @param name 任务名称
     * @param cron cron表达式
     */
    public void changeCron(String name, String cron) {
        if (!CronExpression.isValidExpression(cron)) {
            String errMsg = StrUtil.format("修改定时任务[{}], cron表达式[{}]不合法", name, cron);
            log.error(errMsg);
            throw new RuntimeException(errMsg);
        }
        stopCron(name);
        ScheduledFuture<?> future = defaultTaskScheduler.schedule(taskGroup.get(name), new CronTrigger(cron));
        futureGroup.put(name, future);
        log.info(StrUtil.format("定时任务[{}({})]变更成功", name, cron));
    }
}

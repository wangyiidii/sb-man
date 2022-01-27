package cn.yiidii.sb.common.config;

import com.alibaba.fastjson.JSONObject;
import java.util.concurrent.ThreadPoolExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * 线程池配置
 *
 * @author ed w
 * @since 1.0
 */
@Slf4j
@Configuration
@EnableAsync
public class ThreadPoolConfig {

    /**
     * CPU个数
     */
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    /**
     * 核心线程数（默认线程数）
     */
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    /**
     * 最大线程数
     */
    private static final int MAX_POOL_SIZE = CPU_COUNT * 2 + 1;
    /**
     * 允许线程空闲时间（单位：默认为秒）
     */
    private static final int KEEP_ALIVE_TIME = 10;
    /**
     * 缓冲队列大小
     */
    private static final int QUEUE_CAPACITY = 100;
    /**
     * 线程池名前缀
     */
    private static final String SCHEDULED_EXECUTOR_NAME_PREFIX = "Async-scheduledExecutor-%s";
    private static final String ASYNC_EXECUTOR_NAME_PREFIX = "Async-Executor-%s";

    /**
     * 解决定时任务和websocket同时使用报错问题
     *
     * @return TaskScheduler
     */
    @Bean("defaultTaskScheduler")
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(10);
        taskScheduler.initialize();
        return taskScheduler;
    }

    /**
     * 使@Scheduled的cron支持配置
     *
     * @return PropertySourcesPlaceholderConfigurer
     */
    @Bean
    public PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    /**
     * 定时任务线程池
     */
    @Bean("scheduledExecutor")
    public ThreadPoolTaskExecutor scheduledExecutor() {
        ThreadPoolTaskExecutor executor = constructor(SCHEDULED_EXECUTOR_NAME_PREFIX);
        log.info(String.format("初始化定时任务线程池: %s", JSONObject.toJSONString(executor)));
        return executor;
    }

    /**
     * 通用线程池
     */
    @Bean("asyncExecutor")
    public ThreadPoolTaskExecutor asyncExecutor() {
        ThreadPoolTaskExecutor executor = constructor(ASYNC_EXECUTOR_NAME_PREFIX);
        log.info(String.format("初始化通用线程池: %s", JSONObject.toJSONString(executor)));
        return executor;
    }

    private ThreadPoolTaskExecutor constructor(String scheduledExecutorNamePrefix) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CORE_POOL_SIZE);
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        executor.setQueueCapacity(QUEUE_CAPACITY);
        executor.setKeepAliveSeconds(KEEP_ALIVE_TIME);
        executor.setThreadNamePrefix(scheduledExecutorNamePrefix);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}

package cn.yiidii.sb.support;

import cn.hutool.core.io.watch.SimpleWatcher;
import cn.hutool.core.io.watch.WatchMonitor;
import cn.hutool.core.io.watch.watchers.DelayWatcher;
import cn.hutool.core.util.StrUtil;
import cn.yiidii.pigeon.common.core.util.SpringContextHolder;
import cn.yiidii.sb.common.prop.SbSystemProperties;
import cn.yiidii.sb.service.RobotCacheService;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * 配置文件监听
 *
 * @author ed w
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SystemConfigFileListener implements InitializingBean {

    private final RobotCacheService robotCacheService;
    private final SbSystemProperties systemProperties;

    @Override
    public void afterPropertiesSet() throws Exception {
        // 启动文件监听
        SimpleWatcher simpleWatcher = new SimpleWatcher() {
            @Override
            public void onModify(WatchEvent<?> event, Path currentPath) {
                String update = systemProperties.update(false);
                try {
                    if (StrUtil.isNotBlank(update)) {
                        log.info("配置文件变更: {}", update);
                        robotCacheService.startTimerTask();
                    }
                } catch (Exception e) {
                    // ignore
                }
            }

            @Override
            public void onDelete(WatchEvent<?> event, Path currentPath) {
                log.info("配置文件删除, 系统停止");
                AbstractApplicationContext ctx = (AbstractApplicationContext) SpringContextHolder.getApplicationContext();
                ctx.registerShutdownHook();
            }

            @Override
            public void onOverflow(WatchEvent<?> event, Path currentPath) {
                log.info("配置文件删除, 系统停止");
                AbstractApplicationContext ctx = (AbstractApplicationContext) SpringContextHolder.getApplicationContext();
                ctx.registerShutdownHook();
            }
        };
        // 延迟处理监听事件
        WatchMonitor.createAll(new ClassPathResource(SbSystemProperties.CONFIG_FILE_NAME).getURL(), new DelayWatcher(simpleWatcher, 1000)).start();
    }

}

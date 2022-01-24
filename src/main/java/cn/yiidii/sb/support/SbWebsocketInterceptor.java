package cn.yiidii.sb.support;

import cn.hutool.core.util.StrUtil;
import cn.yiidii.sb.common.constant.SbRobotConstant;
import cn.yiidii.sb.common.prop.SbSystemProperties;
import cn.yiidii.sb.common.prop.SbSystemProperties.RobotConfig;
import cn.yiidii.sb.service.RobotCacheService;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.lz1998.pbbot.handler.BotSessionInterceptor;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

/**
 * websocket拦截器
 *
 * @author ed w
 * @since 1.0
 */
@Slf4j
@Primary
@Component
@RequiredArgsConstructor
public class SbWebsocketInterceptor extends BotSessionInterceptor {

    private static final Map<Long, AtomicInteger> TIME_LOG_CACHE = Maps.newConcurrentMap();
    private static final Integer MAX_LOG_TIMES = 3;

    private final SbSystemProperties systemProperties;
    private final RobotCacheService robotCacheService;

    @SneakyThrows
    @Override
    public boolean checkSession(@NotNull WebSocketSession session) {
        HttpHeaders headers = session.getHandshakeHeaders();
        Long botId = Long.valueOf(headers.getFirst(SbRobotConstant.X_SELF_ID));
        List<RobotConfig> robotConfigs = systemProperties.getRobot();
        List<Long> configuredQqs = robotConfigs.stream().map(RobotConfig::getQq).distinct().collect(Collectors.toList());
        if (!configuredQqs.contains(botId)) {
            // 初始化次数
            AtomicInteger atomicTimes = TIME_LOG_CACHE.get(botId);
            if (Objects.isNull(atomicTimes)) {
                atomicTimes = new AtomicInteger(1);
                TIME_LOG_CACHE.put(botId, atomicTimes);
            }
            int times = atomicTimes.getAndIncrement();
            if (times <= MAX_LOG_TIMES) {
                log.info(StrUtil.format("机器人(QQ: {})上线, 但系统未配置该机器人QQ, 断开连接!({})", botId, MAX_LOG_TIMES + 1 - times));
            }
            session.close();
            return false;
        }
        TIME_LOG_CACHE.remove(botId);

        log.info(StrUtil.format("机器人(QQ: {})上线", botId));
        robotCacheService.addOrUpdateRobot(botId);
        return true;
    }
}

package cn.yiidii.sb.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.DesensitizedUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.yiidii.pigeon.common.core.util.SpringContextHolder;
import cn.yiidii.sb.common.constant.SbScheduleNameConstant;
import cn.yiidii.sb.common.prop.SbSystemProperties;
import cn.yiidii.sb.common.prop.SbSystemProperties.RobotConfig;
import cn.yiidii.sb.model.bo.LtPhoneCache;
import cn.yiidii.sb.model.bo.QqCache;
import cn.yiidii.sb.model.bo.RobotCache;
import cn.yiidii.sb.model.cmd.LtCommand;
import cn.yiidii.sb.model.dto.LtAccountInfo;
import cn.yiidii.sb.util.ScheduleTaskUtil;
import com.alibaba.fastjson.JSONObject;
import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lz1998.pbbot.bot.Bot;
import net.lz1998.pbbot.bot.BotContainer;
import onebot.OnebotApi.GetFriendListResp;
import onebot.OnebotApi.GetFriendListResp.Friend;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * SingleQqCache service
 *
 * @author ed w
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RobotCacheService {

    private static final String DATA_PATH = System.getProperty("user.dir") + File.separator + "data" + File.separator + "data.json";
    private static final Map<Long, RobotCache> DATA_CACHE = new ConcurrentHashMap<>();

    private static boolean INIT = false;

    private final ChinaUnicomService chinaUnicomService;
    private final BotContainer botContainer;
    private final SbSystemProperties systemProperties;
    private final ScheduleTaskUtil scheduleTaskUtil;

    @PostConstruct
    public void init() {
        try {
            String s = FileUtil.readString(DATA_PATH, StandardCharsets.UTF_8);
            JSONObject cacheJo = JSONUtil.toBean(JSONUtil.parseObj(s), JSONObject.class);
            cacheJo.forEach((k, v) -> DATA_CACHE.put(Long.valueOf(k), BeanUtil.toBean(JSONObject.parseObject(v.toString()), RobotCache.class)));
        } catch (IORuntimeException e) {
            FileUtil.writeString("{}", DATA_PATH, StandardCharsets.UTF_8);
        }
        INIT = true;
    }

    public void addOrUpdateRobot(Long robotQq) {
        Map<Long, RobotConfig> configuredRobotMap = systemProperties.getRobot().stream().collect(Collectors.toMap(RobotConfig::getQq, e -> e));
        RobotConfig robotConfig = configuredRobotMap.get(robotQq);
        if (!configuredRobotMap.containsKey(robotQq)) {
            return;
        }
        if (DATA_CACHE.containsKey(robotConfig.getQq())) {
            DATA_CACHE.get(robotQq).setName(robotConfig.getName());
        } else {
            DATA_CACHE.put(robotQq, new RobotCache(robotQq, robotConfig.getName()));
        }
    }

    public Bot getBot(Long robotId) {
        Map<Long, RobotConfig> configuredRobots = systemProperties.getRobot().stream().collect(Collectors.toMap(RobotConfig::getQq, e -> e, (e1, e2) -> e2));
        if (!configuredRobots.containsKey(robotId)) {
            throw new RuntimeException(StrUtil.format("未配置机器人(QQ: {})", robotId));
        }
        Map<Long, Bot> bots = botContainer.getBots();
        Bot bot = bots.get(robotId);
        if (Objects.isNull(bot) || !bot.getBotSession().isOpen()) {
            throw new RuntimeException(StrUtil.format("机器人(QQ: {})未连接", robotId));
        }
        return bot;
    }

    public RobotConfig getRobotConfig(Long robotId) {
        Map<Long, RobotConfig> configuredRobots = systemProperties.getRobot().stream().collect(Collectors.toMap(RobotConfig::getQq, e -> e, (e1, e2) -> e2));
        if (!configuredRobots.containsKey(robotId)) {
            throw new RuntimeException(StrUtil.format("未配置机器人(QQ: {})", robotId));
        }
        Map<Long, Bot> bots = botContainer.getBots();
        Bot bot = bots.get(robotId);
        if (Objects.isNull(bot) || !bot.getBotSession().isOpen()) {
            throw new RuntimeException(StrUtil.format("机器人(QQ: {})未连接", robotId));
        }
        return configuredRobots.get(robotId);
    }

    public Map<Long, Friend> getFriendMap(Long robotId) {
        GetFriendListResp friendList = this.getBot(robotId).getFriendList();
        return friendList.getFriendList().stream().collect(Collectors.toMap(Friend::getUserId, e -> e, (e1, e2) -> e2));
    }

    public Friend getFriend(Long robotId, Long friendId) {
        return this.getFriendMap(robotId).get(friendId);
    }

    public void sendPrivateMessage(Long robotId, Long friendId, String message) {
        if (StrUtil.isBlank(message)) {
            throw new RuntimeException("不能发送空白消息");
        }
        this.getBot(robotId).sendPrivateMsg(friendId, message, true);
    }


    public void addOrUpdateQqInfo(Long robotQq, Long friendQq) {
        if (Objects.isNull(friendQq)) {
            return;
        }
        // 检查robot缓存
        if (!DATA_CACHE.containsKey(robotQq)) {
            return;
        }
        // 在robot新增|更新一个QQ
        Friend friend = this.getFriend(robotQq, friendQq);
        long qq = friend.getUserId();
        Map<Long, QqCache> qqCacheMap = DATA_CACHE.get(robotQq).getQqCacheMap();
        QqCache qqCache = qqCacheMap.get(qq);
        if (Objects.isNull(qqCache)) {
            qqCache = new QqCache(robotQq, qq, friend.getNickname(), friend.getRemark());
            qqCacheMap.put(qq, qqCache);
        } else {
            qqCache.setRobotQq(robotQq);
            qqCache.setQq(qq);
            qqCache.setNick(friend.getNickname());
            qqCache.setRemark(friend.getRemark());
        }
    }

    public void addOrUpdateLtPhone(Long robotQq, Long sender, LtPhoneCache ltPhone) {
        QqCache qqCache = this.getQq(robotQq, sender);
        Map<String, LtPhoneCache> ltPhoneCacheMap = qqCache.getLtPhoneCache();
        ltPhoneCacheMap.put(ltPhone.getPhone(), ltPhone);
        // 执行一次监控
        this.monitor(qqCache, true);
    }

    public void setupLtPhoneThreshold(Long robotQq, Long sender, String phone, Long threshold) {
        QqCache qqCache = this.getQq(robotQq, sender);
        LtPhoneCache ltPhoneCache = qqCache.getLtPhoneCache().get(phone);
        if (Objects.isNull(ltPhoneCache)) {
            throw new RuntimeException(StrUtil.format("未绑定手机号{}", DesensitizedUtil.mobilePhone(phone)));
        }
        ltPhoneCache.setThreshold(threshold);
    }

    public void switchMonitorStatus(Long robotQq, Long sender, String phone, boolean monitor) {
        QqCache qqCache = this.getQq(robotQq, sender);
        LtPhoneCache ltPhoneCache = qqCache.getLtPhoneCache().get(phone);
        if (Objects.isNull(ltPhoneCache)) {
            throw new RuntimeException(StrUtil.format("未绑定手机号{}", DesensitizedUtil.mobilePhone(phone)));
        }
        ltPhoneCache.setMonitor(monitor);
    }

    public String getFlowStatistic(Long robotQq, Long sender, String phone) {
        QqCache qqCache = this.getQq(robotQq, sender);
        LtPhoneCache ltPhoneCache = qqCache.getLtPhoneCache().get(phone);
        if (Objects.isNull(ltPhoneCache)) {
            throw new RuntimeException(StrUtil.format("未绑定手机号{}", DesensitizedUtil.mobilePhone(phone)));
        }
        String msg = StrUtil.format("手机：{}\r\n监控：{}\r\n阈值：{}MB\r\n合计：{}MB\r\n已用：{}MB\r\n已免：{}MB\r\n跳点：{}MB\r\n统计：{}\r\n刷新：{}",
                DesensitizedUtil.mobilePhone(ltPhoneCache.getPhone()),
                ltPhoneCache.isMonitor() ? "已开启" : "未开启",
                ltPhoneCache.getThreshold(),
                ltPhoneCache.getSum(),
                ltPhoneCache.getUse(),
                ltPhoneCache.getFree(),
                ltPhoneCache.getOffset(),
                DateUtil.formatLocalDateTime(ltPhoneCache.getStatisticTime()),
                DateUtil.formatLocalDateTime(ltPhoneCache.getLastMonitorTime())
        );
        return msg;
    }

    public void resetOffset(Long robotQq, Long sender, String phone) {
        QqCache qqCache = this.getQq(robotQq, sender);
        LtPhoneCache ltPhoneCache = qqCache.getLtPhoneCache().get(phone);
        if (Objects.isNull(ltPhoneCache)) {
            throw new RuntimeException(StrUtil.format("未绑定手机号{}", DesensitizedUtil.mobilePhone(phone)));
        }
        ltPhoneCache.setOffset(BigDecimal.ZERO);
    }


    public List<String> getBoundPhone(Long robotQq, Long sender) {
        QqCache qqCache = this.getQq(robotQq, sender);
        Map<String, LtPhoneCache> ltPhoneCacheMap = qqCache.getLtPhoneCache();
        Assert.isTrue(CollUtil.isNotEmpty(ltPhoneCacheMap), StrUtil.format("未绑定手机号，请获取验证码绑定！【{}】", LtCommand.SEND_SMS.getDisplayName()));
        return ltPhoneCacheMap.keySet().stream().collect(Collectors.toList());
    }

    public List<String> unbindPhone(Long robotQq, Long sender, List<String> phones) {
        Assert.isTrue(CollUtil.isNotEmpty(phones), "请输入需要解绑的手机号");

        QqCache qqCache = this.getQq(robotQq, sender);
        Map<String, LtPhoneCache> ltPhoneCacheMap = qqCache.getLtPhoneCache();

        Assert.isTrue(CollUtil.isNotEmpty(ltPhoneCacheMap), StrUtil.format("未绑定手机号，请获取验证码绑定！【{}】", LtCommand.SEND_SMS.getDisplayName()));

        List<String> successPhones = new ArrayList<>();
        phones.forEach(p -> {
            if (ltPhoneCacheMap.containsKey(p)) {
                successPhones.add(p);
                ltPhoneCacheMap.remove(p);
            }
        });
        return successPhones;
    }

    /**
     * 启动定时任务
     */
    public void startTimerTask() {
        scheduleTaskUtil.startCron(SbScheduleNameConstant.ROBOT_TIMER_MONITOR, () -> timerMonitor(), systemProperties.getLtMonitorCron());
        scheduleTaskUtil.startCron(SbScheduleNameConstant.ROBOT_TIMER_PERSIST_CACHE_DATA, () -> timerPersistCacheData(), "0/20 * * * * ?");
    }

    /**
     * 定时序列化数据
     */
    private void timerPersistCacheData() {
        if (!INIT) {
            return;
        }
        Thread.currentThread().setName(String.format(Thread.currentThread().getName(), SbScheduleNameConstant.ROBOT_TIMER_PERSIST_CACHE_DATA));
        String prettyJa = JSONUtil.toJsonPrettyStr(DATA_CACHE);
        FileUtil.writeString(prettyJa, DATA_PATH, StandardCharsets.UTF_8);
    }

    /**
     * 联通定时监控
     */
    private void timerMonitor() {
        if (!INIT) {
            return;
        }
        Thread.currentThread().setName(String.format(Thread.currentThread().getName(), SbScheduleNameConstant.ROBOT_TIMER_MONITOR));
        ThreadPoolTaskExecutor executor = SpringContextHolder.getBean("asyncExecutor", ThreadPoolTaskExecutor.class);
        List<QqCache> qqCaches = DATA_CACHE.values().stream()
                .map(robotCache -> robotCache.getQqCacheMap().values())
                .flatMap(Collection::stream)
                .filter(qq -> CollUtil.isNotEmpty(qq.getLtPhoneCache()))
                .collect(Collectors.toList());
        log.info("定时监控联通跳点, 本次监控{}个账户", qqCaches.size());
        qqCaches.forEach(qq -> executor.execute(() -> this.monitor(qq, false)));
    }

    private QqCache getQq(Long robotQq, Long friendQq) {
        // 先更新下friend, 如果friend有的话
        this.addOrUpdateQqInfo(robotQq, friendQq);
        // 到这里QQ缓存一定存在
        return DATA_CACHE.get(robotQq).getQqCacheMap().get(friendQq);
    }

    private void monitor(QqCache qqCache, boolean force) {
        Long robotQq = qqCache.getRobotQq();
        Long friendQq = qqCache.getQq();
        qqCache.getLtPhoneCache().values().forEach(ltPhoneCache -> {
            boolean canExecute = force || ltPhoneCache.isMonitor();
            if (!canExecute) {
                return;
            }
            LtAccountInfo ltAccountInfo = chinaUnicomService.getAccountInfo(ltPhoneCache.getCookie());
            BigDecimal sum = ltAccountInfo.getSummary().getSum();
            BigDecimal free = ltAccountInfo.getSummary().getFreeFlow();
            BigDecimal use = sum.subtract(free);

            // 默认-1的，如果超了，代表是第一次执行，不通知
            boolean firstTime = ltPhoneCache.getSum().compareTo(BigDecimal.ZERO) < 0;
            // 和上一次的差值
            BigDecimal diff = use.subtract(ltPhoneCache.getUse());
            // 先保存
            ltPhoneCache.setSum(sum);
            ltPhoneCache.setFree(free);
            ltPhoneCache.setUse(use);
            if (!firstTime) {
                // 不是第一次才进行累加
                ltPhoneCache.setOffset(ltPhoneCache.getOffset().add(diff));
            }
            ltPhoneCache.setLastMonitorTime(LocalDateTime.now());
            ltPhoneCache.setStatisticTime(DateUtil.parseLocalDateTime(ltAccountInfo.getTime()));
            boolean over = ltPhoneCache.getOffset().subtract(new BigDecimal(ltPhoneCache.getThreshold())).compareTo(BigDecimal.ZERO) > 0;
            // 通知
            if (over && !firstTime) {
                // 流量统计描述
                String flowStatistic = this.getFlowStatistic(robotQq, friendQq, ltPhoneCache.getPhone());
                flowStatistic = "❗❗❗跳点告警❗❗❗\r\n".concat(flowStatistic).concat(StrUtil.format("\r\n\r\n\r\n请先发送【{}】重置跳点, 否则该信息将会一直提醒!", LtCommand.RESET_OFFSET.getDisplayName()));
                // 发送消息
                this.sendPrivateMessage(robotQq, friendQq, flowStatistic);
                // 日志
                log.info(StrUtil.format("robotId, {}, QQ: {}, 手机号: {}, 存在跳点({}M/{}M), 已私聊告警", robotQq, friendQq, ltPhoneCache.getPhone(), ltPhoneCache.getOffset(), ltPhoneCache.getThreshold()));

            }
        });
    }
}

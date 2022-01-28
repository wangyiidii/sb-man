package cn.yiidii.sb.cmd.handler.impl;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.DesensitizedUtil;
import cn.hutool.core.util.PhoneUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.yiidii.pigeon.common.core.base.R;
import cn.yiidii.pigeon.common.core.constant.StringPool;
import cn.yiidii.pigeon.common.strategy.annotation.HandlerType;
import cn.yiidii.sb.cmd.handler.ICmdHandler;
import cn.yiidii.sb.model.bo.LtPhoneCache;
import cn.yiidii.sb.cmd.cmd.impl.LtCommand;
import cn.yiidii.sb.cmd.cmd.CmdBeanName;
import cn.yiidii.sb.model.form.TelecomLoginForm;
import cn.yiidii.sb.service.ChinaUnicomService;
import cn.yiidii.sb.service.RobotCacheService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onebot.OnebotEvent.PrivateMessageEvent;
import onebot.OnebotEvent.PrivateMessageEvent.Sender;
import org.springframework.stereotype.Component;

/**
 * ILTHandler
 *
 * @author ed w
 * @since 1.0
 */
public interface ILtHandler extends ICmdHandler<PrivateMessageEvent> {

    TimedCache<Long, String> TIMED_CACHE = CacheUtil.newTimedCache(5 * 60 * 1000);

    /**
     * 帮助
     *
     * @author ed w
     * @since 1.0
     */
    @Component(CmdBeanName.LT_HELP)
    @HandlerType(bizCode = CmdBeanName.LT_HELP, beanName = CmdBeanName.LT_HELP)
    class Help implements ILtHandler {

        @Override
        public R<?> handle(PrivateMessageEvent event) {
            String cmds = Arrays.stream(LtCommand.values())
                    .filter(e -> !e.equals(LtCommand.HELP))
                    .map(e -> "# ".concat(e.getDisplayName()))
                    .collect(Collectors.joining(StrUtil.CRLF));
            return R.ok(null, cmds);
        }
    }

    /**
     * 发送验证码
     *
     * @author ed w
     * @since 1.0
     */
    @Component(CmdBeanName.LT_SEND_SMS)
    @RequiredArgsConstructor
    @HandlerType(bizCode = CmdBeanName.LT_SEND_SMS, beanName = CmdBeanName.LT_SEND_SMS)
    class SendSms implements ILtHandler {

        private final ChinaUnicomService chinaUnicomService;

        @Override
        public R<?> handle(PrivateMessageEvent event) {
            String phone = ReUtil.getGroup0("\\d{11}", event.getRawMessage());
            if (!PhoneUtil.isPhone(phone)) {
                throw new RuntimeException("手机号码格式不正确");
            }
            TelecomLoginForm telecomLoginForm = new TelecomLoginForm()
                    .setMobile(phone);
            String msg = chinaUnicomService.sendRandomNum(telecomLoginForm);
            TIMED_CACHE.put(event.getSender().getUserId(), phone);
            return R.ok(null, msg);
        }
    }

    /**
     * 登录
     *
     * @author ed w
     * @since 1.0
     */
    @Slf4j
    @Component(CmdBeanName.LT_LOGIN)
    @RequiredArgsConstructor
    @HandlerType(bizCode = CmdBeanName.LT_LOGIN, beanName = CmdBeanName.LT_LOGIN)
    class Login implements ILtHandler {

        private final ChinaUnicomService chinaUnicomService;
        private final RobotCacheService robotCacheService;

        @Override
        public R<?> handle(PrivateMessageEvent event) {
            Sender friend = event.getSender();
            String phone = TIMED_CACHE.get(friend.getUserId());
            if (StrUtil.isEmpty(phone)) {
                throw new RuntimeException(StrUtil.format("请先获取验证码【{}】", LtCommand.SEND_SMS.getDisplayName()));
            }
            String code = ReUtil.getGroup0("\\d{4,6}", event.getRawMessage());
            TelecomLoginForm telecomLoginForm = new TelecomLoginForm()
                    .setMobile(phone)
                    .setPassword(code);
            R<?> r;
            try {
                r = chinaUnicomService.randomLogin(telecomLoginForm);
            } catch (Exception e) {
                log.info("qq: {}, phone: {}, code: {}, errMsg: {}", friend.getUserId(), phone, code, e.getMessage());
                throw new RuntimeException(e.getMessage());
            }
            if (r.getCode() == 0) {
                // 成功获取到ck
                TIMED_CACHE.remove(event.getSender().getUserId());
                JSONObject jo = JSONObject.parseObject((JSON.toJSONString(r.getData())));
                String cookie = jo.getString("cookieStr");
                // 添加
                LtPhoneCache ltPhoneCache = new LtPhoneCache(phone, cookie);
                robotCacheService.addOrUpdateLtPhone(event.getSelfId(), friend.getUserId(), ltPhoneCache);
                // 返回流量统计信息
                log.info("{}({}), phone: {}, 加入监控", friend.getUserId(), event.getSender().getNickname(), phone);
                // 返回登陆成功
                return R.ok(null, "登录成功");
            } else {
                return R.failed("登录失败，请联系管理员");
            }
        }
    }

    @Slf4j
    @Component(CmdBeanName.LT_SET_THRESHOLD)
    @RequiredArgsConstructor
    @HandlerType(bizCode = CmdBeanName.LT_SET_THRESHOLD, beanName = CmdBeanName.LT_SET_THRESHOLD)
    class SetThreshold implements ILtHandler {

        private final RobotCacheService robotCacheService;

        @Override
        public R<?> handle(PrivateMessageEvent event) {
            // e.g. 15600001234-10
            String phoneAndThreshold = ReUtil.getGroup0("[\\d-]+", event.getRawMessage());
            String phone = phoneAndThreshold.split(StringPool.DASH)[0];
            long threshold;
            try {
                threshold = Long.parseLong(phoneAndThreshold.split(StringPool.DASH)[1]);
            } catch (NumberFormatException e) {
                throw new RuntimeException("阈值格式错误。例: 设置阈值15600000000-10】");
            }
            if (threshold <= 0) {
                throw new RuntimeException("阈值必须大于0");
            }

            robotCacheService.setupLtPhoneThreshold(event.getSelfId(), event.getSender().getUserId(), phone, threshold);
            log.info("robot: {}, qq: {}, phone: {}, 设置跳点阈值为:{}M", event.getSelfId(), event.getSender().getUserId(), phone, threshold);
            return R.ok(null, StrUtil.format("设置{}跳点阈值为{}M成功!", DesensitizedUtil.mobilePhone(phone), threshold));
        }
    }

    @Slf4j
    @RequiredArgsConstructor
    @Component(CmdBeanName.LT_START_MONITOR)
    @HandlerType(bizCode = CmdBeanName.LT_START_MONITOR, beanName = CmdBeanName.LT_START_MONITOR)
    class StartMonitor implements ILtHandler {

        private final RobotCacheService robotCacheService;

        @Override
        public R<?> handle(PrivateMessageEvent event) {
            String message = event.getRawMessage();
            String phone = ReUtil.getGroup0("\\d{11}", message);
            if (!PhoneUtil.isPhone(phone)) {
                throw new RuntimeException("手机号格式不正确");
            }
            robotCacheService.switchMonitorStatus(event.getSelfId(), event.getSender().getUserId(), phone, true);
            log.info("robot: {}, qq: {}, phone: {}, 开启流量监控", event.getSelfId(), event.getSender().getUserId(), phone);
            return R.ok(null, StrUtil.format("{}开启流量监控成功", DesensitizedUtil.mobilePhone(phone)));
        }
    }

    @Slf4j
    @RequiredArgsConstructor
    @Component(CmdBeanName.LT_STOP_MONITOR)
    @HandlerType(bizCode = CmdBeanName.LT_STOP_MONITOR, beanName = CmdBeanName.LT_STOP_MONITOR)
    class StopMonitor implements ILtHandler {

        private final RobotCacheService robotCacheService;

        @Override
        public R<?> handle(PrivateMessageEvent event) {
            String message = event.getRawMessage();
            String phone = ReUtil.getGroup0("\\d{11}", message);
            if (!PhoneUtil.isPhone(phone)) {
                throw new RuntimeException("手机号格式不正确");
            }
            robotCacheService.switchMonitorStatus(event.getSelfId(), event.getSender().getUserId(), phone, false);
            log.info("robot: {}, qq: {}, phone: {}, 关闭流量监控", event.getSelfId(), event.getSender().getUserId(), phone);
            return R.ok(null, StrUtil.format("{}关闭流量监控成功", DesensitizedUtil.mobilePhone(phone)));
        }
    }

    @Slf4j
    @RequiredArgsConstructor
    @Component(CmdBeanName.LT_FLOW_STATISTIC)
    @HandlerType(bizCode = CmdBeanName.LT_FLOW_STATISTIC, beanName = CmdBeanName.LT_FLOW_STATISTIC)
    class FlowStatistic implements ILtHandler {

        private final RobotCacheService robotCacheService;

        @Override
        public R<?> handle(PrivateMessageEvent event) {
            String message = event.getRawMessage();
            String phone = ReUtil.getGroup0("\\d{11}", message);
            if (!PhoneUtil.isPhone(phone)) {
                throw new RuntimeException("手机号格式不正确");
            }
            String flowStatistic = robotCacheService.getFlowStatistic(event.getSelfId(), event.getSender().getUserId(), phone);
            return R.ok(null, flowStatistic);
        }
    }

    @Slf4j
    @RequiredArgsConstructor
    @Component(CmdBeanName.LT_RESET_OFFSET)
    @HandlerType(bizCode = CmdBeanName.LT_RESET_OFFSET, beanName = CmdBeanName.LT_RESET_OFFSET)
    class ResetOffset implements ILtHandler {

        private final RobotCacheService robotCacheService;

        @Override
        public R<?> handle(PrivateMessageEvent event) {
            String message = event.getRawMessage();
            String phone = ReUtil.getGroup0("\\d{11}", message);
            if (!PhoneUtil.isPhone(phone)) {
                throw new RuntimeException("手机号格式不正确");
            }
            robotCacheService.resetOffset(event.getSelfId(), event.getSender().getUserId(), phone);
            log.info("robot: {}, qq: {}, phone: {}, 重置跳点统计", event.getSelfId(), event.getSender().getUserId(), phone);
            return R.ok(null, StrUtil.format("{}重置跳点统计成功", DesensitizedUtil.mobilePhone(phone)));
        }
    }

    @Slf4j
    @RequiredArgsConstructor
    @Component(CmdBeanName.LT_CAT_BOUND_PHONE)
    @HandlerType(bizCode = CmdBeanName.LT_CAT_BOUND_PHONE, beanName = CmdBeanName.LT_CAT_BOUND_PHONE)
    class CatBoundPhone implements ILtHandler {

        private final RobotCacheService robotCacheService;

        @Override
        public R<?> handle(PrivateMessageEvent event) {
            List<String> boundPhone = robotCacheService.getBoundPhone(event.getSelfId(), event.getSender().getUserId());
            String msg = "已绑定手机号".concat(StrPool.CRLF).concat(boundPhone.stream().map(DesensitizedUtil::mobilePhone).collect(Collectors.joining(StrPool.CRLF)));
            return R.ok(null, msg);
        }
    }

    @Slf4j
    @RequiredArgsConstructor
    @Component(CmdBeanName.LT_UNBIND_PHONE)
    @HandlerType(bizCode = CmdBeanName.LT_UNBIND_PHONE, beanName = CmdBeanName.LT_UNBIND_PHONE)
    class UnbindPhone implements ILtHandler {

        private final RobotCacheService robotCacheService;

        @Override
        public R<?> handle(PrivateMessageEvent event) {
            String message = event.getRawMessage();
            String phone = ReUtil.getGroup0("\\d{11}", message);
            if (!PhoneUtil.isPhone(phone)) {
                throw new RuntimeException("手机号格式不正确");
            }

            List<String> successPhones = robotCacheService.unbindPhone(event.getSelfId(), event.getSender().getUserId(), Lists.newArrayList(phone));
            if (successPhones.size() != 1) {
                throw new RuntimeException(StrUtil.format("未绑定手机号{}", DesensitizedUtil.mobilePhone(phone)));
            }
            log.info("robot:{}, qq: {}, phone: {}, 解绑手机", event.getSelfId(), event.getSender().getUserId(), phone);
            return R.ok(null, StrUtil.format("{}解绑手机成功", DesensitizedUtil.mobilePhone(phone)));
        }
    }

    @Slf4j
    @RequiredArgsConstructor
    @Component(CmdBeanName.LT_REMOVE_ALL_PHONE)
    @HandlerType(bizCode = CmdBeanName.LT_REMOVE_ALL_PHONE, beanName = CmdBeanName.LT_REMOVE_ALL_PHONE)
    class UnbindAllPhone implements ILtHandler {

        private final RobotCacheService robotCacheService;

        @Override
        public R<?> handle(PrivateMessageEvent event) {
            List<String> boundPhone = robotCacheService.getBoundPhone(event.getSelfId(), event.getSender().getUserId());
            List<String> successPhones = robotCacheService.unbindPhone(event.getSelfId(), event.getSender().getUserId(), boundPhone);
            log.info("robot:{}, qq: {}, 解绑所有手机: ", event.getSelfId(), event.getSender().getUserId(), successPhones.stream().map(DesensitizedUtil::mobilePhone).collect(Collectors.joining(StrPool.COMMA)));

            successPhones.add(0, "成功解绑手机");
            String msg = CollUtil.join(successPhones, StrPool.CRLF);
            return R.ok(null, msg);
        }
    }
}

package cn.yiidii.sb.cmd.cmd.impl;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.yiidii.sb.cmd.handler.ICmdHandler;
import cn.yiidii.sb.cmd.cmd.ICommand;
import cn.yiidii.sb.cmd.cmd.CmdBeanName;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 联通命令
 *
 * @author ed w
 * @since 1.0
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum LtCommand implements ICommand {

    HELP("联通机器人", "联通机器人", CmdBeanName.LT_HELP),
    SEND_SMS("登录手厅+手机号", "登录手厅\\d{11}", CmdBeanName.LT_SEND_SMS),
    LOGIN("登录验证+验证码", "登录验证\\d{4,6}", CmdBeanName.LT_LOGIN),
    SETUP_THRESHOLD("设置阈值+手机号+-阈值【例: 设置阈值15600000000-10】", "设置阈值[\\d-]+", CmdBeanName.LT_SET_THRESHOLD),
    START_MONITOR("开启监控+手机号", "开启监控\\d{11}", CmdBeanName.LT_START_MONITOR),
    STOP_MONITOR("关闭监控+手机号", "关闭监控\\d{11}", CmdBeanName.LT_STOP_MONITOR),
    FLOW_STATISTIC("流量统计+手机号", "流量统计\\d{11}", CmdBeanName.LT_FLOW_STATISTIC),
    RESET_OFFSET("重置跳点+手机号", "重置跳点\\d{11}", CmdBeanName.LT_RESET_OFFSET),
    UNBIND_PHONE("解绑手机+手机号", "解绑手机\\d{11}", CmdBeanName.LT_UNBIND_PHONE),
    REMOVE_ALL_PHONE("解绑所有手机", "解绑所有手机", CmdBeanName.LT_REMOVE_ALL_PHONE),
    CAT_BOUND_PHONE("查看绑定手机", "查看绑定手机", CmdBeanName.LT_CAT_BOUND_PHONE),
    ;

    String displayName;
    String regx;
    String beanName;

    public static LtCommand get(String command) {
        return getOrDefault(command, null);
    }

    public static LtCommand getOrDefault(String type, LtCommand dft) {
        return Stream.of(values()).parallel().filter(item -> StrUtil.equalsAnyIgnoreCase(type, item.regx)).findAny().orElse(dft);
    }

    public static LtCommand match(String command) {
        return Stream.of(values())
                .parallel()
                .filter(item -> ReUtil.isMatch(item.regx, command))
                .findAny().orElse(null);
    }

    @Override
    public Class getClazz() {
        return ICmdHandler.class;
    }
}

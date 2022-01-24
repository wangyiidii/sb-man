package cn.yiidii.sb.model.cmd;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.yiidii.sb.cmdhandler.ICmdHandler;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * command
 *
 * @author ed w
 * @since 1.0
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum LtCommand implements ICommand {

    HELP("联通机器人", "联通机器人", LtCmdBeanName.LT_HELP),
    SEND_SMS("登录手厅+手机号", "登录手厅\\d{11}", LtCmdBeanName.LT_SEND_SMS),
    LOGIN("登录验证+验证码", "登录验证\\d{4,6}", LtCmdBeanName.LT_LOGIN),
    SETUP_THRESHOLD("设置阈值+手机号+-阈值【例: 设置阈值15600000000-10】", "设置阈值[\\d-]+", LtCmdBeanName.LT_SET_THRESHOLD),
    START_MONITOR("开启监控+手机号", "开启监控\\d{11}", LtCmdBeanName.LT_START_MONITOR),
    STOP_MONITOR("关闭监控+手机号", "关闭监控\\d{11}", LtCmdBeanName.LT_STOP_MONITOR),
    FLOW_STATISTIC("流量统计+手机号", "流量统计\\d{11}", LtCmdBeanName.LT_FLOW_STATISTIC),
    RESET_OFFSET("重置跳点+手机号", "重置跳点\\d{11}", LtCmdBeanName.LT_RESET_OFFSET),
    UNBIND_PHONE("解绑手机+手机号", "解绑手机\\d{11}", LtCmdBeanName.LT_UNBIND_PHONE),
    REMOVE_ALL_PHONE("解绑所有手机", "解绑所有手机", LtCmdBeanName.LT_REMOVE_ALL_PHONE),
    CAT_BOUND_PHONE("查看绑定手机", "查看绑定手机", LtCmdBeanName.LT_CAT_BOUND_PHONE),

//    STATUS("运行状态", "运行状态", CommandBeanName.LT_STATUS),
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

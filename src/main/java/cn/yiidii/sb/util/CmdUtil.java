package cn.yiidii.sb.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.StaticLog;
import cn.yiidii.pigeon.common.core.util.SpringContextHolder;
import cn.yiidii.sb.common.prop.SbSystemProperties.RobotConfig;
import cn.yiidii.sb.cmd.cmd.ICommand;
import cn.yiidii.sb.service.RobotCacheService;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;

/**
 * 命令工具类
 *
 * @author ed w
 * @since 1.0
 */
@Slf4j
@UtilityClass
public class CmdUtil {

    private static final List<ICommand> CMD_CACHE = Lists.newArrayList();

    static {
        Reflections reflections = new Reflections();
        reflections.getSubTypesOf(ICommand.class).stream().distinct().forEach(e -> {
            try {
                Arrays.stream(e.getEnumConstants()).forEach(CMD_CACHE::add);
            } catch (Exception ex) {
                StaticLog.error(StrUtil.format("cannot instance {} when load ICommand", e.getName()));
            }
        });
        List<String> log = CMD_CACHE.stream().map(c -> StrUtil.format("displayName: {}, regx: {}, class: {}", c.getDisplayName(), c.getRegx(), c.getClass().getName())).distinct().collect(Collectors.toList());
        StaticLog.info("加载到{}个机器人指令: {}", CMD_CACHE.size(), JSONObject.toJSONString(log));
    }

    public ICommand match(String content) {
        List<ICommand> matches = CMD_CACHE.stream().filter(cmd -> ReUtil.isMatch(cmd.getRegx(), content)).distinct().collect(Collectors.toList());
        return CollUtil.isEmpty(matches) ? null : RandomUtil.randomEle(matches);
    }

    public String getWelcomeWord(Long robotQq) {
        RobotCacheService robotCacheService = SpringContextHolder.getBean(RobotCacheService.class);
        RobotConfig robotConfig = robotCacheService.getRobotConfig(robotQq);
        List<ICommand> menu = CmdUtil.getMenu();
        String welcome = menu.stream().map(e -> "# ".concat(e.getDisplayName())).collect(Collectors.joining(StrUtil.CRLF));
        return StrUtil.format("欢迎添加{}!\r\n请输入命令立即尝试一下吧~ \r\n\r\n", robotConfig.getName()).concat(welcome);
    }

    public List<ICommand> getMenu() {
        return CMD_CACHE.stream().filter(cmd -> cmd.toString().equals("HELP")).distinct().collect(Collectors.toList());
    }

    public static void main(String[] args) {

    }

}

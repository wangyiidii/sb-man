package cn.yiidii.sb.plugin;

import cn.hutool.core.util.StrUtil;
import cn.yiidii.pigeon.common.core.base.R;
import cn.yiidii.pigeon.common.core.util.SpringContextHolder;
import cn.yiidii.sb.cmd.cmd.impl.ZhiSiCommand;
import cn.yiidii.sb.cmd.handler.ICmdHandler;
import cn.yiidii.sb.cmd.cmd.ICommand;
import cn.yiidii.sb.cmd.handler.impl.IZhiSiHandler;
import cn.yiidii.sb.util.CmdUtil;
import cn.yiidii.sb.util.ZhiSiUtil;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import net.lz1998.pbbot.bot.Bot;
import net.lz1998.pbbot.bot.BotPlugin;
import onebot.OnebotEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

/**
 * HelloPlugin
 *
 * @author ed w
 * @since 1.0
 */
@Slf4j
@Component
public class PrivateMessageReplyPlugin extends BotPlugin {

    @Override
    public int onPrivateMessage(@NotNull Bot bot, @NotNull OnebotEvent.PrivateMessageEvent event) {
        // 消息
        String messageStr = event.getRawMessage().trim();

        // 如果开启了知思AI对话，优先对话
        boolean contains = IZhiSiHandler.timedCache.containsKey(event.getUserId());
        if (contains && !StrUtil.equals(messageStr, ZhiSiCommand.AI_END.getDisplayName())) {
            // 思知AI机器人
            IZhiSiHandler.timedCache.put(event.getUserId(), null);
            String aiReply = ZhiSiUtil.aiReply(String.valueOf(event.getSender().getUserId()), messageStr);
            bot.sendPrivateMsg(event.getUserId(), aiReply, false);
            return MESSAGE_IGNORE;
        }

        // 匹配ICommand下的枚举正则，如匹配不到，则采用思知机器人对话
        ICommand command = CmdUtil.match(messageStr);
        log.debug("[{}], QQ: {}, message: {}, cmd: {}, class: {}", event.getSelfId(), event.getUserId(), event.getRawMessage(), Objects.isNull(command) ? "none-cmd" : command, Objects.isNull(command) ? "none-class" : command.getClass().getName());
        if (Objects.nonNull(command)) {
            // 获取Bean，并执行
            ICmdHandler handler = SpringContextHolder.getBean(command.getBeanName(), command.getClazz());
            if (Objects.isNull(handler)) {
                return MESSAGE_IGNORE;
            }
            R result;
            try {
                result = handler.handle(event);
            } catch (Exception e) {
                result = R.failed(e.getMessage());
            }
            // 回复消息
            String msg = result.getMsg();
            bot.sendPrivateMsg(event.getUserId(), msg, false);
        }
        return MESSAGE_IGNORE;
    }

}

package cn.yiidii.sb.plugin;

import cn.yiidii.pigeon.common.core.base.R;
import cn.yiidii.pigeon.common.core.util.SpringContextHolder;
import cn.yiidii.sb.cmdhandler.ICmdHandler;
import cn.yiidii.sb.model.cmd.ICommand;
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
public class LtReplyPlugin extends BotPlugin {

    @Override
    public int onPrivateMessage(@NotNull Bot bot, @NotNull OnebotEvent.PrivateMessageEvent event) {

        // 消息
        String messageStr = event.getRawMessage().trim();

        // 匹配ICommand下的枚举正则，如匹配不到，则采用思知机器人对话
        ICommand command = CmdUtil.match(messageStr);
        log.debug("[{}], QQ: {}, message: {}, cmd: {}, class: {}", event.getSelfId(), event.getUserId(), event.getRawMessage(), Objects.isNull(command) ? "none-cmd" : command, Objects.isNull(command) ? "none-class" : command.getClass().getName());
        if (Objects.isNull(command)) {
            // 思知AI机器人
            String aiReply = ZhiSiUtil.aiReply(String.valueOf(event.getSender().getUserId()), messageStr);
            bot.sendPrivateMsg(event.getUserId(), aiReply, false);
        } else {
            // 获取Bean，并执行
            ICmdHandler handler = SpringContextHolder.getBean(command.getBeanName(), command.getClazz());
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

package cn.yiidii.sb.plugin;

import cn.yiidii.sb.util.CmdUtil;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import net.lz1998.pbbot.bot.Bot;
import net.lz1998.pbbot.bot.BotPlugin;
import onebot.OnebotEvent.FriendRequestEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

/**
 * 好友申请
 *
 * @author ed w
 * @since 1.0
 */
@Slf4j
@Component
public class FriendRequestEventPlugin extends BotPlugin {

    @Override
    public int onFriendRequest(@NotNull Bot bot, @NotNull FriendRequestEvent event) {
        long userId = event.getUserId();
        String comment = event.getComment();
        bot.setFriendAddRequest(event.getFlag(), true, "");
        String welcomeWord = CmdUtil.getWelcomeWord(bot.getSelfId());
        try {
            TimeUnit.SECONDS.sleep(2L);
        } catch (InterruptedException e) {
        }
        bot.sendPrivateMsg(userId, welcomeWord, false);
        log.info("[{}] - QQ: {}添加了机器人, 验证信息: {}", bot.getSelfId(), userId, comment);
        return MESSAGE_IGNORE;
    }
}

package cn.yiidii.sb.cmd.handler.impl;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.util.StrUtil;
import cn.yiidii.pigeon.common.core.base.R;
import cn.yiidii.pigeon.common.strategy.annotation.HandlerType;
import cn.yiidii.sb.cmd.cmd.CmdBeanName;
import cn.yiidii.sb.cmd.cmd.impl.LtCommand;
import cn.yiidii.sb.cmd.cmd.impl.ZhiSiCommand;
import cn.yiidii.sb.cmd.handler.ICmdHandler;
import java.util.Arrays;
import java.util.stream.Collectors;
import onebot.OnebotEvent.PrivateMessageEvent;
import org.springframework.stereotype.Component;

/**
 * 知思
 *
 * @author ed w
 * @since 1.0
 */
public interface IZhiSiHandler extends ICmdHandler<PrivateMessageEvent> {

    TimedCache<Long, String> timedCache = CacheUtil.newTimedCache(1000 * 60 * 2);

    @Component(CmdBeanName.ZHISI_HELP)
    @HandlerType(bizCode = CmdBeanName.ZHISI_HELP, beanName = CmdBeanName.ZHISI_HELP)
    class Help implements IZhiSiHandler {

        @Override
        public R<?> handle(PrivateMessageEvent event) {
            String cmd = Arrays.stream(ZhiSiCommand.values())
                    .filter(e -> !e.equals(ZhiSiCommand.HELP))
                    .map(e -> "# ".concat(e.getDisplayName()))
                    .collect(Collectors.joining(StrUtil.CRLF));
            return R.ok(null, cmd);
        }
    }

    @Component(CmdBeanName.ZHISI_AI_BEGIN)
    @HandlerType(bizCode = CmdBeanName.ZHISI_AI_BEGIN, beanName = CmdBeanName.ZHISI_AI_BEGIN)
    class ZhiSiBegin implements IZhiSiHandler {

        @Override
        public R<?> handle(PrivateMessageEvent event) {
            timedCache.put(event.getUserId(), null);
            return R.ok(null, StrUtil.format("想聊点什么呢？\r\n\r\n（要想结束输入【{}】即可，若2分钟未说话自动结束）", ZhiSiCommand.AI_END.getDisplayName()));
        }
    }

    @Component(CmdBeanName.ZHISI_AI_END)
    @HandlerType(bizCode = CmdBeanName.ZHISI_AI_END, beanName = CmdBeanName.ZHISI_AI_END)
    class ZhiSiEnd implements IZhiSiHandler {

        @Override
        public R<?> handle(PrivateMessageEvent event) {
            timedCache.remove(event.getUserId());
            return R.ok(null, "Bye~");
        }
    }

}

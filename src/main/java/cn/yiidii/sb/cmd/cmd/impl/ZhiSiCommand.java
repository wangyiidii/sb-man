package cn.yiidii.sb.cmd.cmd.impl;

import cn.yiidii.sb.cmd.cmd.CmdBeanName;
import cn.yiidii.sb.cmd.cmd.ICommand;
import cn.yiidii.sb.cmd.handler.ICmdHandler;
import cn.yiidii.sb.cmd.handler.impl.IZhiSiHandler;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author ed w
 * @since 1.0
 */
@Getter
@AllArgsConstructor
public enum ZhiSiCommand implements ICommand {

    HELP("AI聊天", "AI聊天", CmdBeanName.ZHISI_HELP),
    AI_BEGIN("开启AI聊天", "开启AI聊天", CmdBeanName.ZHISI_AI_BEGIN),
    AI_END("关闭AI聊天", "关闭AI聊天", CmdBeanName.ZHISI_AI_END),
    ;

    String displayName;
    String regx;
    String beanName;

    @Override
    public Class<? extends ICmdHandler<?>> getClazz() {
        return IZhiSiHandler.class;
    }
}

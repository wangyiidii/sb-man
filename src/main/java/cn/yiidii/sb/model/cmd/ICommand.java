package cn.yiidii.sb.model.cmd;

import cn.yiidii.sb.cmdhandler.ICmdHandler;

/**
 * ICommand
 *
 * @author ed w
 * @since 1.0
 */
public interface ICommand {

    /**
     * getClazz
     *
     * @return Class
     */
    Class<? extends ICmdHandler<?>> getClazz();

    String getDisplayName();

    String getRegx();

    String getBeanName();
}

package cn.yiidii.sb.cmd.cmd;

import cn.yiidii.sb.cmd.handler.ICmdHandler;

/**
 * ICommand 接口
 *
 * @author ed w
 * @since 1.0
 */
public interface ICommand {

    /**
     * 处理器的class
     *
     * @return Class
     */
    Class<? extends ICmdHandler<?>> getClazz();

    /**
     * 显示名称，即将来在QQ的显示的名称
     *
     * @return 显示名称
     */
    String getDisplayName();

    /**
     * 匹配命令的正则表达式
     *
     * @return 匹配命令的正则表达式
     */
    String getRegx();

    /**
     * 处理该命令的Bean名称
     *
     * @return Bean名称
     */
    String getBeanName();
}

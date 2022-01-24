package cn.yiidii.sb.cmdhandler;

import cn.yiidii.pigeon.common.core.base.R;
import cn.yiidii.pigeon.common.strategy.handler.AbstractHandler;
import java.io.Serializable;

/**
 * cmd抽象接口
 *
 * @author ed w
 * @since 1.0
 */
public interface ICmdHandler<T> extends AbstractHandler<R, T>, Serializable {

    /**
     * handle
     *
     * @param t 参数
     * @return
     */
    @Override
    R handle(T t);
}

package cn.yiidii.sb.model.bo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * LtPhoneCache
 *
 * @author ed w
 * @since 1.0
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(of = {"robotQq", "qq", "phone", "cookie", "sum", "use", "free", "offset"})
public class QqCache {

    private Long robotQq;
    private Long qq;
    private String nick;
    private String remark;

    private Map<String, LtPhoneCache> ltPhoneCache;

    public QqCache(Long robotQq, Long qq, String nick, String remark) {
        this.robotQq = robotQq;
        this.qq = qq;
        this.nick = nick;
        this.remark = remark;
        ltPhoneCache = new ConcurrentHashMap<>();
    }
}

package cn.yiidii.sb.model.bo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * RobotCache
 *
 * @author ed w
 * @since 1.0
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
public class RobotCache {

    private Long qq;
    private String name;
    private Map<Long, QqCache> qqCacheMap;

    public RobotCache(Long qq, String name) {
        this.qq = qq;
        this.name = name;
        qqCacheMap = new ConcurrentHashMap<>();
    }

}


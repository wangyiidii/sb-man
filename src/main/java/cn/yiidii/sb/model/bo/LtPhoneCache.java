package cn.yiidii.sb.model.bo;

import cn.yiidii.pigeon.common.core.base.enumeration.Status;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * LtPhoneCache
 *
 * @author ed w
 * @since 1.0
 */
@Data
@Accessors(chain = true)
public class LtPhoneCache {

    private String phone;
    private String cookie;

    private boolean monitor;
    private Long threshold;

    private BigDecimal sum;
    private BigDecimal use;
    private BigDecimal filteredUse;
    private BigDecimal free;
    private BigDecimal offset;

    private LocalDateTime statisticTime;
    private LocalDateTime lastMonitorTime;
    private LocalDateTime createTime;
    private Status status = Status.ENABLED;

    public LtPhoneCache(String phone, String cookie) {
        this.phone = phone;
        this.cookie = cookie;

        this.monitor = false;
        this.threshold = 10L;

        this.sum = new BigDecimal("-1");
        this.use = new BigDecimal("-1");
        this.filteredUse = new BigDecimal("-1");
        this.free = new BigDecimal("-1");
        this.offset = BigDecimal.ZERO;

        this.statisticTime = LocalDateTime.now();
        this.lastMonitorTime = LocalDateTime.now();
        this.createTime = LocalDateTime.now();
    }
}

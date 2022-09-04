package cn.yiidii.sb.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 联通用量差异
 *
 * @author ed w
 * @since 1.0
 */
@Data
@Accessors(chain = true)
public class LtUsageDiffResponseDTO {
    private Integer code;
    private String msg;
    private LtUsageDiff data;
    private LocalDateTime timestamp;

    @Data
    public static class LtUsageDiff {
        private String phoneNumber;
        private String pkgName;

        /**
         * 包内总共用量（包含了定向和通用，如果不知道定向的话，其实是没什么用）
         */
        private BigDecimal sum;
        /**
         * 包内流量
         */
        private Pkg pkg;
        /**
         * 免费流量
         */
        private BigDecimal free;

        /**
         * 跳点
         */
        private BigDecimal diff;

        /**
         * 上次查询时间
         */
        private LocalDateTime lastTime;

    }

    /**
     * 包内流量
     */
    @Data
    @Accessors(chain = true)
    public static class Pkg {

        /**
         * 通用流量
         */
        private BigDecimal generic;

        /**
         * 定向流量
         */
        private BigDecimal direction;
    }
}

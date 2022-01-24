package cn.yiidii.sb.common.prop;

import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * xitongbeizhi
 *
 * @author ed w
 * @since 1.0
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "sb.config")
public class SbSystemProperties {

    private List<RobotConfig> robot;

    @Data
    public static class RobotConfig {

        private String name;
        private Long qq;
    }
}

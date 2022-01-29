package cn.yiidii.sb.common.prop;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * xitongbeizhi
 *
 * @author ed w
 * @since 1.0
 */
@Slf4j
@Data
@Component
public class SbSystemProperties implements InitializingBean {

    public static final String CONFIG_FILE_PATH = System.getProperty("user.dir") + File.separator + "config" + File.separator + "config.json";

    private String ltMonitorCron;
    private List<RobotConfig> robot;

    @Override
    public void afterPropertiesSet() {
        // 根据config.json赋值
        update(true);
    }

    public String update(boolean throwException) {
        try {
            String configStr = FileUtil.readUtf8String(CONFIG_FILE_PATH);
            JSONObject configJo = JSONObject.parseObject(configStr);
            BeanUtil.copyProperties(configJo, this);
            return configJo.toJSONString();
        } catch (Exception e) {
            if (throwException) {
                throw new IllegalArgumentException(StrUtil.format("{}不存在", CONFIG_FILE_PATH));
            }
            log.error("更新配置文件[{}]发生异常, e: {}", CONFIG_FILE_PATH, e.getMessage());
            return null;
        }
    }

    @Data
    public static class RobotConfig {

        private String name;
        private Long qq;
    }
}

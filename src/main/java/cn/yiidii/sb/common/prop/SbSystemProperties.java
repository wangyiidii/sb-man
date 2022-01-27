package cn.yiidii.sb.common.prop;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import java.io.File;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;
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

    public static final String CONFIG_FILE_NAME = "config/config.json";

    private String ltMonitorCron;
    private List<RobotConfig> robot;

    @Override
    public void afterPropertiesSet() {
        // 根据config.json赋值
        update(true);
    }

    public String update(boolean throwException) {
        ClassPathResource configResource = new ClassPathResource(CONFIG_FILE_NAME);
        File configJsonFile;
        try {
            configJsonFile = configResource.getFile();
        } catch (Exception e) {
            if (throwException) {
                throw new IllegalArgumentException(StrUtil.format("{}不存在", CONFIG_FILE_NAME));
            }
            log.error("更新配置文件[{}]发生异常step1, e: {}", CONFIG_FILE_NAME, e.getMessage());
            return null;
        }

        String content = FileUtil.readUtf8String(configJsonFile);
        JSONObject configJo;
        try {
            configJo = JSONObject.parseObject(content);
        } catch (Exception e) {
            if (throwException) {
                throw new IllegalArgumentException(StrUtil.format("config.json.sample 配置格式有误"));
            }
            log.error("更新配置文件[{}]发生异常step2, e: {}", CONFIG_FILE_NAME, e.getMessage());
            return null;
        }
        BeanUtil.copyProperties(configJo, this);
        return configJo.toJSONString();
    }

    @Data
    public static class RobotConfig {

        private String name;
        private Long qq;
    }
}

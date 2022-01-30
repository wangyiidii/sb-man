package cn.yiidii.sb.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpStatus;
import cn.yiidii.pigeon.common.core.constant.StringPool;
import cn.yiidii.pigeon.common.core.util.HttpClientUtil;
import cn.yiidii.pigeon.common.core.util.dto.HttpClientResult;
import cn.yiidii.sb.common.constant.SbScheduleNameConstant;
import cn.yiidii.sb.util.ScheduleTaskUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 系统配置
 *
 * @author ed w
 * @since 1.0.3
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemConfigService {

    private static final String JAR_SUFFIX = ".jar";
    private static final String SYSTEM_CONFIG = System.getProperty("user.dir") + File.separator + "config" + File.separator + "system.config";

    private static boolean INIT = false;

    @Value("${spring.application.name:sb-man}")
    private String applicationName;

    @Value("${spring.application.version:1.0.3}")
    private String applicationVersionStr;

    private final ScheduleTaskUtil scheduleTaskUtil;

    @PostConstruct
    public void init() {
        try {
            FileUtil.readString(SYSTEM_CONFIG, StandardCharsets.UTF_8);
        } catch (IORuntimeException e) {
            FileUtil.writeString("upgrade=false", SYSTEM_CONFIG, StandardCharsets.UTF_8);
        }
        INIT = true;
    }

    /**
     * 定时从github的releases检查系统jar更新
     */
    public void timerCheckIfNeedUpdated() {
        if (!INIT) {
            return;
        }
        JSONObject result;
        try {
            result = this.check();
            if (Objects.isNull(result)) {
                return;
            }
        } catch (Exception e) {
            return;
        }
        Map<String, String> systemConfigMap = FileUtil.readUtf8Lines(SYSTEM_CONFIG).stream()
                .filter(e -> StrUtil.isNotBlank(e) && e.split(StringPool.EQUALS).length == 2)
                .collect(Collectors.toMap(e -> e.split(StringPool.EQUALS)[0], e -> e.split(StringPool.EQUALS)[0], (e1, e2) -> e1));
        result.forEach((k, v) -> systemConfigMap.put(k, String.valueOf(v)));

        Set<String> lines = systemConfigMap.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.toSet());
        FileUtil.writeUtf8Lines(lines, SYSTEM_CONFIG);
    }

    /**
     * 从github releases检查更新jar
     *
     * @return result
     */
    private JSONObject check() {
        HttpClientResult response;
        try {
            response = HttpClientUtil.doGet(StrUtil.format("https://api.github.com/repos/wangyiidii/sb-man/releases/latest"));
        } catch (Exception e) {
            return null;
        }
        if (response.getCode() != HttpStatus.HTTP_OK) {
            return null;
        }
        JSONObject respJo = JSONObject.parseObject(response.getContent());
        JSONArray assets = respJo.getJSONArray("assets");
        Optional<Object> optional = assets.stream().filter(e -> {
            JSONObject asset = (JSONObject) e;
            return asset.getString("name").endsWith(JAR_SUFFIX);
        }).findFirst();
        if (!optional.isPresent()) {
            return null;
        }
        JSONObject result = new JSONObject();

        JSONObject target = (JSONObject) optional.get();
        String githubJarName = target.getString("name");
        String githubVersionStr = githubJarName.replaceAll(applicationName + "-", "").replaceAll(JAR_SUFFIX, "");
        long githubVersion = Long.parseLong(ReUtil.replaceAll(githubVersionStr, "[^0-9]", ""));
        long applicationVersion = Long.parseLong(ReUtil.replaceAll(applicationVersionStr, "[^0-9]", ""));
        if (githubVersion > applicationVersion) {
            result.put("upgrade", true);
            result.put("version", githubVersionStr);
            result.put("url", target.get("browser_download_url"));
            log.info("检测到有需要升级, 已经写入{}, 停止任务", SYSTEM_CONFIG);
            scheduleTaskUtil.stopCron(SbScheduleNameConstant.SYSTEM_CHECK_UPGRADE);
        } else {
            result.put("upgrade", false);
        }
        return result;
    }

}

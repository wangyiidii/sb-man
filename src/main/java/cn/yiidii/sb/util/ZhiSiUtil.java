package cn.yiidii.sb.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import com.alibaba.fastjson.JSONObject;
import lombok.experimental.UtilityClass;

/**
 * 思知工具
 *
 * @author ed w
 * @since 1.0
 */
@UtilityClass
public class ZhiSiUtil {

    public String aiReply(String appId, String spoken) {
        HttpResponse response = HttpRequest.get(StrUtil.format("https://api.ownthink.com/bot?appid={}&userid=user&spoken={}", appId, spoken.trim().replaceAll(" ", "")))
                .execute();
        if (response.getStatus() != HttpStatus.HTTP_OK) {
            return "机器人抽风中";
        }
        return JSONObject.parseObject(response.body()).getJSONObject("data").getJSONObject("info").getString("text");
    }

}

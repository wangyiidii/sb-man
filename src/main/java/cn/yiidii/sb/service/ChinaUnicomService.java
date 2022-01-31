package cn.yiidii.sb.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.yiidii.pigeon.common.core.base.R;
import cn.yiidii.pigeon.common.core.constant.StringPool;
import cn.yiidii.pigeon.common.core.exception.BizException;
import cn.yiidii.sb.model.dto.LtAccountInfo;
import cn.yiidii.sb.model.form.TelecomLoginForm;
import cn.yiidii.sb.util.IpUtil;
import cn.yiidii.sb.util.RSAUtil;
import com.alibaba.fastjson.JSONObject;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * ChinaUnicomService
 *
 * @author ed w
 * @since 1.0
 */
@Service("ChinaUnicomService")
@RequiredArgsConstructor
public class ChinaUnicomService {

    /**
     * 加密公钥
     */
    private static final String CHINA_UNICOM_PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDc+CZK9bBA9IU+gZUOc6FUGu7yO9WpTNB0PzmgFBh96Mg1WrovD1oqZ+eIF4LjvxKXGOdI79JRdve9NPhQo07+uqGQgE4imwNnRx7PFtCRryiIEcUoavuNtuRVoBAm6qdB0SrctgaqGfLgKvZHOnwTjyNqjBUxzMeQlEC2czEMSwIDAQAB";
    /**
     * 获取验证码接口
     */
    private static final String URL_SEND_RANDOM_NUM = "https://m.client.10010.com/mobileService/sendRadomNum.htm";
    /**
     * 登陆接口
     */
    private static final String URL_RANDOM_LOGIN = "https://m.client.10010.com/mobileService/radomLogin.htm";

    private static final String URL_RESOURCE_STATISTIC = "https://m.client.10010.com/servicequerybusiness/operationservice/queryOcsPackageFlowLeftContentRevisedInJune";


    public String sendRandomNum(TelecomLoginForm telecomLoginForm) {
        String mobile = telecomLoginForm.getMobile();
        // 随机数
        int randomSixCode = RandomUtil.randomInt(100000, 1000000);
        String encryptMobile;

        // 拿公钥对（mobile + 随机数）进行RSA加密
        try {
            encryptMobile = RSAUtil.encryptByPubKey(mobile + randomSixCode, CHINA_UNICOM_PUBLIC_KEY);
        } catch (Exception e) {
            throw new BizException("加密手机号时发生异常");
        }

        // 请求参数
        Map<String, Object> params = new HashMap<>(16);
        params.put("mobile", encryptMobile);
        params.put("version", "android@7.0601");
        params.put("keyVersion", "");

        // 请求验证码接口
        HttpResponse response = HttpRequest.post(URL_SEND_RANDOM_NUM).form(params).execute();
        if (response.getStatus() != 200) {
            throw new BizException(String.format("请求验证码失败(%d)", response.getStatus()));
        }

        String body = response.body();
        JSONObject respJo = JSONObject.parseObject(body);

        // 返回tip
        return respJo.getString("rsp_desc");
    }

    public R<?> randomLogin(TelecomLoginForm telecomLoginForm) {
        String mobile = telecomLoginForm.getMobile();
        String password = telecomLoginForm.getPassword();
        String userContent = telecomLoginForm.getUserContent();
        String imageId = telecomLoginForm.getImageId();
        String imei = RandomUtil.randomString(32);
        int randomSixCode = (int) ((Math.random() * 9 + 1) * 100000);
        String encryptMobile;
        String encryptPwd;
        try {
            encryptMobile = RSAUtil.encryptByPubKey(mobile + randomSixCode, CHINA_UNICOM_PUBLIC_KEY);
            encryptPwd = RSAUtil.encryptByPubKey(password + randomSixCode, CHINA_UNICOM_PUBLIC_KEY);
        } catch (Exception e) {
            throw new BizException("加密出现异常");
        }

        Map<String, Object> params = new HashMap<>(16);
        params.put("mobile", encryptMobile);
        params.put("password", encryptPwd);
        params.put("loginStyle", "0");
        params.put("isRemberPwd", "false");
        params.put("provinceChanel", "general");
        params.put("timestamp", DateUtil.format(new Date(), DatePattern.PURE_DATETIME_FORMAT));
        params.put("yw_code", "");
        params.put("deviceOS", "android10");
        params.put("netWay", "WIFI");
        params.put("deviceCode", imei);
        params.put("version", "android@7.0601");
        params.put("deviceId", imei);
        params.put("keyVersion", "");
        params.put("pip", IpUtil.getRandomIp());
        params.put("voice_code", "");
        params.put("appId", RandomUtil.randomString(32));
        params.put("voiceoff_flag", "");
        params.put("deviceModel", "OnePlus");
        params.put("deviceBrand", "ONEPLUS A6000");

        if (StringUtils.isNotBlank(userContent) || StringUtils.isNotBlank(imageId)) {
            params.put("userContent", userContent);
            params.put("imageID", imageId);
            params.put("code", "7");
        }
        HttpResponse resp = HttpRequest.post(URL_RANDOM_LOGIN)
                .header("User-Agent", "")
                .form(params)
                .execute();

        // 解析响应
        int status = resp.getStatus();
        if (status != HttpStatus.HTTP_OK) {
            throw new BizException(String.format("登陆失败(%s)", status));
        }

        JSONObject loginRespJo = JSONObject.parseObject(resp.body());
        String rspCode = loginRespJo.getString("code");
        String rspDesc = loginRespJo.getString("dsc");
        if (StringUtils.equals(rspCode, "7")) {
            // 需要手动输入图片验证码
            return R.ok(loginRespJo, "请输入图片验证码");
        }
        if (!StringUtils.equals(rspCode, "0")) {
            throw new BizException(StringUtils.isBlank(rspDesc) ? "登陆失败" : rspDesc);
        }
        JSONObject resultJo = new JSONObject();
        resultJo.put("chinaUnicomResp", loginRespJo);
        String cookieStr = StringUtils.join(resp.getCookies().stream().map(e -> e.getName().concat(StringPool.EQUALS).concat(e.getValue())).collect(Collectors.toList()), "; ");
        resultJo.put("cookieStr", cookieStr);
        return R.ok(resultJo, "登陆成功");
    }

    public LtAccountInfo getAccountInfo(String cookie) {
        HttpResponse response = HttpRequest.post(URL_RESOURCE_STATISTIC)
                .header(Header.COOKIE, cookie)
                .body("externalSources=&contactCode=&serviceType=&saleChannel=&channelCode=&duanlianjieabc=&ticket=&ticketPhone=&ticketChannel=&userNumber=&language=")
                .execute();
        if (HttpStatus.HTTP_OK != response.getStatus()) {
            throw new BizException("查询用户余量信息失败");
        }
        JSONObject respJo;
        try {
            respJo = JSONObject.parseObject(response.body());
        } catch (Exception e) {
            throw new BizException("响应异常");
        }
        return BeanUtil.toBean(respJo, LtAccountInfo.class);
    }

}

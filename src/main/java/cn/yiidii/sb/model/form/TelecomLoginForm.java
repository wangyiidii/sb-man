package cn.yiidii.sb.model.form;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.Pattern;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 中国联通登录Form类
 *
 * @author YiiDii Wang
 * @create 2020-12-02 21:20
 */
@Data
@Accessors(chain = true)
@ApiModel("运营商登陆表单")
public class TelecomLoginForm {


    /**
     * 手机号码
     */
    @Pattern(regexp = "\\d{11}", message = "手机号码格式不正确")
    @ApiModelProperty(value = "手机号")
    private String mobile;

    /**
     * 验证码
     */
    @Pattern(regexp = "\\d{4,6}", message = "验证码格式不正确")
    @ApiModelProperty(value = "验证码")
    private String password;

    /**
     * 图片验证码
     */
    @ApiModelProperty(value = "图形验证码")
    private String userContent;

    /**
     * 图片验证码的imageId
     */
    @ApiModelProperty(value = "图片验证码的ID")
    private String imageId;

}

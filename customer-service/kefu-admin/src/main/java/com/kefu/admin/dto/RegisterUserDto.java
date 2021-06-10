package com.kefu.admin.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author jurui
 * @date 2020-05-21
 */
@Data
public class RegisterUserDto implements Serializable {

    private static final long serialVersionUID = 6445149916048588249L;
    /**
     * 账号
     */
    @NotBlank(message = "账号不能为空")
    @Length(min = 5, max = 18, message = "账号长度为5-18位")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Length(min = 6, max = 18, message = "密码长度为6-18位")
    private String password;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 昵称
     */
    private String nickname;
}

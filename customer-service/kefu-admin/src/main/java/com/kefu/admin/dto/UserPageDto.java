package com.kefu.admin.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.kefu.admin.entity.User;
import com.kefu.admin.entity.enums.UserStatusEnum;
import com.kefu.common.db.dto.PageDto;
import com.kefu.common.util.EnumValueDeserializer;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * 分页查询vo，暂时这样写，后面会封装基类和枚举，并处理一些约束
 *
 * @author jurui
 * @date 2020-05-20
 */
@Data
@ToString(callSuper = true)
public class UserPageDto extends PageDto<User> implements Serializable {

    private static final long serialVersionUID = -4864447250966063233L;

    /**
     * 账号
     */
    private String username;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 状态 [0.禁用 1.正常 2.删除]
     */
    @JSONField(serializeUsing = EnumValueDeserializer.class, deserializeUsing = EnumValueDeserializer.class)
    private UserStatusEnum status;

    /**
     * 团队编号
     */
    private Integer teamId;
}

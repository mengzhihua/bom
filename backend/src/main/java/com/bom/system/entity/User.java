package com.bom.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.bom.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 系统用户。role: ADMIN(全部) / BUYER(采购员, 不含用户维护) / SUPPLIER(供应商门户, 限本人 supplierCode 数据) / VIEWER(只读)
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bom_user")
public class User extends BaseEntity {
    public static final String ADMIN = "ADMIN";
    public static final String ENGINEER = "ENGINEER";
    public static final String PLANNER = "PLANNER";
    public static final String VIEWER = "VIEWER";

    private String username;
    /** 写入时可携带明文密码，响应中永不输出 */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    private String realName;
    private String role;
    /** SUPPLIER 角色绑定的供应商编码（bom_supplier.code） */
    private String supplierCode;
    private Integer status;
    private LocalDateTime lastLoginAt;
}

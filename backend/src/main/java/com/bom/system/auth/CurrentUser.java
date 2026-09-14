package com.bom.system.auth;

import com.bom.common.BizException;
import com.bom.system.entity.User;

/** 当前请求线程绑定的登录用户 */
public final class CurrentUser {
    private static final ThreadLocal<User> HOLDER = new ThreadLocal<>();

    private CurrentUser() {
    }

    public static User get() {
        return HOLDER.get();
    }

    static void set(User user) {
        HOLDER.set(user);
    }

    static void clear() {
        HOLDER.remove();
    }

    /** 当前用户绑定的供应商编码（若有） */
    public static String supplierCode() {
        User u = get();
        return u == null ? null : u.getSupplierCode();
    }

    /** 保留兼容入口 */
    public static void checkSupplier(String supplierCode) {
        String mine = supplierCode();
        if (mine != null && !mine.equals(supplierCode)) {
            throw new BizException("无权操作其他供应商的数据");
        }
    }

    /** 仅管理员可执行的系统级动作 */
    public static void requireBuyerSide() {
        User u = get();
        if (u != null && User.VIEWER.equals(u.getRole())) {
            throw new BizException("只读账号无权执行此操作");
        }
    }
}

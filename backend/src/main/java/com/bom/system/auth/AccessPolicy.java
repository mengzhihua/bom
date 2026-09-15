package com.bom.system.auth;

import com.bom.system.entity.User;
import java.util.regex.Pattern;

/**
* 角色访问策略（按 HTTP 方法 + 路径判断）：
* <ul>
*   <li>任何登录用户可读（GET）</li>
*   <li>VIEWER 不可写</li>
*   <li>ENGINEER 可写零件、EBOM、ECR/ECN；PLANNER 可写 MBOM、工位、工厂并实施 ECN</li>
*   <li>ADMIN 无限制</li>
* </ul>
* /api/auth/** 属于登录用户自助操作（改密、登出），所有角色均可。
*/
public final class AccessPolicy {
    private static final Pattern READ_ONLY_BOM_OPERATION = Pattern.compile(
            "^/api/boms/\\d+/"
                    + "(configure|tree|explode|summarized|rollup|by-station|export)$");
    private static final Pattern BOM_COMPARE = Pattern.compile(
            "^/api/boms/compare$");

    private AccessPolicy() {
    }
    public static boolean allows(String role, String method, String path) {
        if (User.ADMIN.equals(role)) {
            return true;
        }
        if (path.startsWith("/api/system/")) {
            return false;
        }
        if ("GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method)) {
            return true;
        }
        if (path.startsWith("/api/auth/")) {
            return true;
        }
        if (("GET".equalsIgnoreCase(method)
                || "POST".equalsIgnoreCase(method))
                && isReadOnlyBomOperation(path)) {
            return true;
        }
        if (User.ENGINEER.equals(role)) {
            return engineerWrite(path);
        }
        if (User.PLANNER.equals(role)) {
            return path.startsWith("/api/boms")
                    || path.startsWith("/api/process")
                    || plannerMasterWrite(path)
                    || path.startsWith("/api/ecns")
                    || path.startsWith("/api/integration");
        }
        return false;
    }

    private static boolean plannerMasterWrite(String path) {
        return path.startsWith("/api/master/plants")
                || path.startsWith("/api/master/workstations");
    }

    private static boolean isReadOnlyBomOperation(String path) {
        return READ_ONLY_BOM_OPERATION.matcher(path).matches()
                || BOM_COMPARE.matcher(path).matches();
    }

    private static boolean engineerWrite(String path) {
        return path.startsWith("/api/parts")
                || path.startsWith("/api/boms")
                || path.startsWith("/api/ecrs")
                || path.startsWith("/api/ecns")
                || path.startsWith("/api/integration")
                || path.startsWith("/api/master");
    }
}

package com.tyyd.framework.dat.management.web.support.csrf;

import javax.servlet.http.HttpServletRequest;

/**
 * 配置在 velocity tools 中
 *
 * <input type="hidden" name="csrfToken" value="$csrfTool.getToken($request)"/>
 *
 */
public class CSRFTool {
    public static String getToken(HttpServletRequest request) {
        return CSRFTokenManager.getToken(request.getSession());
    }
}

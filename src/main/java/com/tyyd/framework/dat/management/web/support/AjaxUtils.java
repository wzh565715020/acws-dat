package com.tyyd.framework.dat.management.web.support;

import javax.servlet.http.HttpServletRequest;

public class AjaxUtils {

    public static boolean isAjaxRequest(HttpServletRequest request) {
        String requestedWith = request.getHeader("X-Requested-With");
        return requestedWith != null && "XMLHttpRequest".equals(requestedWith);
    }

}

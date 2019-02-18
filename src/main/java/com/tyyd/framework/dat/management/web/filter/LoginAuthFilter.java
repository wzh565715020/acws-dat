package com.tyyd.framework.dat.management.web.filter;

import org.springframework.util.AntPathMatcher;

import com.tyyd.framework.dat.core.commons.utils.Base64;
import com.tyyd.framework.dat.core.commons.utils.StringUtils;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class LoginAuthFilter implements Filter {
    private static final String AUTH_PREFIX = "Basic ";
    private AntPathMatcher pathMatcher = new AntPathMatcher();

    private String username = "admin";

    private String password = "admin";

    private String[] excludedURLArray;

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        String excludedURLs = filterConfig.getInitParameter("excludedURLs");
        if (StringUtils.isNotEmpty(excludedURLs)) {
            String[] arr = excludedURLs.split(",");
            excludedURLArray = new String[arr.length];
            for (int i = 0; i < arr.length; i++) {
                excludedURLArray[i] = StringUtils.trim(arr[i]);
            }
        }
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (isExclude(httpRequest.getRequestURI())) {
            chain.doFilter(request, response);
            return;
        }

        String authorization = httpRequest.getHeader("authorization");
        if (null != authorization && authorization.length() > AUTH_PREFIX.length()) {
            authorization = authorization.substring(AUTH_PREFIX.length(), authorization.length());
            if ((username + ":" + password).equals(new String(Base64.decodeFast(authorization)))) {
                authenticateSuccess(httpResponse);
                chain.doFilter(httpRequest, httpResponse);
            } else {
                needAuthenticate(httpRequest, httpResponse);
            }
        } else {
            needAuthenticate(httpRequest, httpResponse);
        }
    }

    private boolean isExclude(String path) {
        if (excludedURLArray != null) {
            for (String page : excludedURLArray) {
                //判断是否在过滤url中
                if (pathMatcher.match(page, path)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void authenticateSuccess(final HttpServletResponse response) {
        response.setStatus(200);
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-store");
        response.setDateHeader("Expires", 0);
    }

    private void needAuthenticate(final HttpServletRequest request, final HttpServletResponse response) {
        response.setStatus(401);
        response.setHeader("Cache-Control", "no-store");
        response.setDateHeader("Expires", 0);
        response.setHeader("WWW-authenticate", AUTH_PREFIX + "Realm=\"dat admin need auth\"");
    }

    @Override
    public void destroy() {
    }
}

package com.tyyd.framework.dat.management.web.support;

import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import com.tyyd.framework.dat.core.json.JSON;
import com.tyyd.framework.dat.core.logger.Logger;
import com.tyyd.framework.dat.core.logger.LoggerFactory;
import com.tyyd.framework.dat.management.web.vo.RestfulResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionHandlerResolver implements HandlerExceptionResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger("[DAT-Admin]");

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {

        // ajax
        if (AjaxUtils.isAjaxRequest(request)) {
            PrintWriter writer = null;
            try {
                writer = response.getWriter();
                RestfulResponse restfulResponse = new RestfulResponse();
                restfulResponse.setSuccess(false);
                StringWriter sw = new StringWriter();
                ex.printStackTrace(new PrintWriter(sw));
                restfulResponse.setMsg(sw.toString());
                String json = JSON.toJSONString(restfulResponse);
                assert json != null;
                writer.write(json);
                writer.flush();
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        } else {
            LOGGER.error(ex.getMessage(), ex);
//            request.setAttribute("message", ex.getMessage());
//            return new ModelAndView("common/error");
        }
        return null;
    }
}


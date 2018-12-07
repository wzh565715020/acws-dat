package com.tyyd.framework.dat.management.web.support;

import com.tyyd.framework.dat.management.web.vo.RestfulResponse;

public class Builder {


    public static RestfulResponse build(boolean success, String msg) {
        RestfulResponse response = new RestfulResponse();
        response.setSuccess(success);
        response.setMsg(msg);
        return response;
    }

    public static RestfulResponse build(boolean success) {
        RestfulResponse response = new RestfulResponse();
        response.setSuccess(success);
        return response;
    }
}

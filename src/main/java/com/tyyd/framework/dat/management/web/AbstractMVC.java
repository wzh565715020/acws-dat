package com.tyyd.framework.dat.management.web;

import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import com.tyyd.framework.dat.management.web.support.DateEditor;
import com.tyyd.framework.dat.management.web.support.MapEditor;

import java.util.Date;
import java.util.Map;

public class AbstractMVC {

    @InitBinder
    protected void initBinder(ServletRequestDataBinder binder) throws Exception {
        //对于需要转换为Date类型的属性，使用DateEditor进行处理
        binder.registerCustomEditor(Date.class, new DateEditor());
        binder.registerCustomEditor(Map.class, "extParams", new MapEditor());
    }

}

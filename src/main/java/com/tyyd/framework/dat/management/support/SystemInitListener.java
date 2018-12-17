package com.tyyd.framework.dat.management.support;

import org.apache.log4j.PropertyConfigurator;

import com.tyyd.framework.dat.core.commons.file.FileUtils;
import com.tyyd.framework.dat.core.commons.utils.StringUtils;
import com.tyyd.framework.dat.core.json.JSONFactory;
import com.tyyd.framework.dat.core.logger.LoggerFactory;
import com.tyyd.framework.dat.core.spi.SpiExtensionKey;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class SystemInitListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {

        String confPath = servletContextEvent.getServletContext().getInitParameter("dat.admin.config.path");
        if (StringUtils.isNotEmpty(confPath)) {
            System.out.println("dat.admin.config.path : " + confPath);
        }
        AppConfigurer.load(confPath);

        String jsonAdapter = AppConfigurer.getProperty("configs." + SpiExtensionKey.DAT_JSON);
        if (StringUtils.isNotEmpty(jsonAdapter)) {
            JSONFactory.setJSONAdapter(jsonAdapter);
        }

        String loggerAdapter = AppConfigurer.getProperty("configs." + SpiExtensionKey.DAT_LOGGER);
        if (StringUtils.isNotEmpty(loggerAdapter)) {
            LoggerFactory.setLoggerAdapter(loggerAdapter);
        }

        String log4jPath = confPath + "/log4j.properties";
        if (FileUtils.exist(log4jPath)) {
            //  log4j 配置文件路径
            PropertyConfigurator.configure(log4jPath);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
    }
}

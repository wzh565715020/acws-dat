package com.tyyd.framework.dat.management.support;

import org.apache.log4j.PropertyConfigurator;

import com.tyyd.framework.dat.core.commons.file.FileUtils;
import com.tyyd.framework.dat.core.commons.utils.StringUtils;
import com.tyyd.framework.dat.core.json.JSONFactory;
import com.tyyd.framework.dat.core.logger.LoggerFactory;
import com.tyyd.framework.dat.core.spi.SpiExtensionKey;
import com.tyyd.framework.dat.management.monitor.MonitorAgentStartup;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class SystemInitListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {

        String confPath = servletContextEvent.getServletContext().getInitParameter("lts.admin.config.path");
        if (StringUtils.isNotEmpty(confPath)) {
            System.out.println("lts.admin.config.path : " + confPath);
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

        boolean monitorAgentEnable = Boolean.valueOf(AppConfigurer.getProperty("lts.monitorAgent.enable", "true"));
        if (monitorAgentEnable) {
            String ltsMonitorCfgPath = confPath;
            if (StringUtils.isEmpty(ltsMonitorCfgPath)) {
                ltsMonitorCfgPath = this.getClass().getResource("/").getPath();
            }
            MonitorAgentStartup.start(ltsMonitorCfgPath);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        MonitorAgentStartup.stop();
    }
}

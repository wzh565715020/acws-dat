package com.tyyd.framework.dat.management.monitor;

import org.apache.log4j.PropertyConfigurator;

import com.tyyd.framework.dat.core.commons.file.FileUtils;
import com.tyyd.framework.dat.core.commons.utils.Assert;
import com.tyyd.framework.dat.core.commons.utils.StringUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class MonitorCfgLoader {

    public static MonitorCfg load(String confPath) {

        String cfgPath = confPath + "/lts-monitor.cfg";
        String log4jPath = confPath + "/log4j.properties";

        Properties conf = new Properties();
        File file = new File(cfgPath);
        InputStream is = null;
        try {
            is = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new CfgException("can not find " + cfgPath);
        }
        try {
            conf.load(is);
        } catch (IOException e) {
            throw new CfgException("Read " + cfgPath + " error.", e);
        }

        MonitorCfg cfg = new MonitorCfg();
        try {
            String registryAddress = conf.getProperty("registryAddress");
            Assert.hasText(registryAddress, "registryAddress can not be null.");
            cfg.setRegistryAddress(registryAddress);

            String clusterName = conf.getProperty("clusterName");
            Assert.hasText(clusterName, "clusterName can not be null.");
            cfg.setClusterName(clusterName);

            String bindIp = conf.getProperty("bindIp");
            if (StringUtils.isNotEmpty(clusterName)) {
                cfg.setBindIp(bindIp);
            }

            Map<String, String> configs = new HashMap<String, String>();
            for (Map.Entry<Object, Object> entry : conf.entrySet()) {
                String key = entry.getKey().toString();
                if (key.startsWith("configs.")) {
                    String value = entry.getValue() == null ? null : entry.getValue().toString();
                    configs.put(key.replace("configs.", ""), value);
                }
            }

            cfg.setConfigs(configs);
        } catch (Exception e) {
            throw new CfgException(e);
        }

        if (FileUtils.exist(log4jPath)) {
            //  log4j 配置文件路径
            PropertyConfigurator.configure(log4jPath);
        }

        return cfg;
    }

}

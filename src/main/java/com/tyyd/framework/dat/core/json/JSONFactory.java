package com.tyyd.framework.dat.core.json;

import com.tyyd.framework.dat.core.commons.utils.StringUtils;
import com.tyyd.framework.dat.core.json.fastjson.FastJSONAdapter;
import com.tyyd.framework.dat.core.json.jackson.JacksonJSONAdapter;
import com.tyyd.framework.dat.core.logger.Logger;
import com.tyyd.framework.dat.core.logger.LoggerFactory;
import com.tyyd.framework.dat.core.spi.ServiceLoader;

public class JSONFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSONFactory.class);

    private static volatile JSONAdapter JSON_ADAPTER;

    static {
        String json = System.getProperty("dat.json");
        if ("fastjson".equals(json)) {
            setJSONAdapter(new FastJSONAdapter());
        } else if ("jackson".equals(json)) {
            setJSONAdapter(new JacksonJSONAdapter());
        } else {
            try {
                setJSONAdapter(new FastJSONAdapter());
            } catch (Throwable ignored) {
                try {
                    setJSONAdapter(new JacksonJSONAdapter());
                } catch (Throwable ignored2) {
                }
            }
        }
    }

    public static void setJSONAdapter(String jsonAdapter) {
        if (StringUtils.isNotEmpty(jsonAdapter)) {
            setJSONAdapter(ServiceLoader.load(JSONAdapter.class, jsonAdapter));
        }
    }

    public static JSONAdapter getJSONAdapter() {
        return JSONFactory.JSON_ADAPTER;
    }

    public static void setJSONAdapter(JSONAdapter jsonAdapter) {
        if (jsonAdapter != null) {
            LOGGER.info("Using JSON lib " + jsonAdapter.getName());
            JSONFactory.JSON_ADAPTER = jsonAdapter;
        }
    }
}

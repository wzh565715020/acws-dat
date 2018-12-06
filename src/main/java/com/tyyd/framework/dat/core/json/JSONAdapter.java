package com.tyyd.framework.dat.core.json;

import com.tyyd.framework.dat.core.spi.SpiExtensionKey;
import com.tyyd.framework.dat.core.spi.SPI;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

@SPI(key = SpiExtensionKey.LTS_JSON, dftValue = "fastjson")
public interface JSONAdapter {

    public String getName();

    public <T> T parse(String json, Type type);

    public String toJSONString(Object obj);

    public JSONObject toJSONObject(Object obj);

    public JSONArray toJSONArray(Object obj);

    public JSONArray parseArray(String json);

    public JSONObject parseObject(String json);

    public JSONObject newJSONObject();

    public JSONObject newJSONObject(Map<String, Object> map);

    public JSONObject newJSONObject(int initialCapacity);

    public JSONArray newJSONArray();

    public JSONArray newJSONArray(List<Object> list);

    public JSONArray newJSONArray(int initialCapacity);

}

package com.tyyd.framework.dat.core.json.ltsjson;

import com.tyyd.framework.dat.core.json.JSONAdapter;
import com.tyyd.framework.dat.core.json.JSONArray;
import com.tyyd.framework.dat.core.json.JSONObject;

import java.lang.reflect.Type;
import java.util.*;

public class LtsJSONAdapter implements JSONAdapter {

    @Override
    public String getName() {
        return "ltsjson";
    }

    @Override
    public <T> T parse(String json, Type type) {
        return com.tyyd.framework.dat.json.JSONObject.parseObject(json, type);
    }

    @Override
    public String toJSONString(Object obj) {
        return com.tyyd.framework.dat.json.JSONObject.toJSONString(obj);
    }

    @Override
    public JSONObject toJSONObject(Object obj) {
        com.tyyd.framework.dat.json.JSONObject jsonObject = new com.tyyd.framework.dat.json.JSONObject(obj);
        return new LtsJSONObject(jsonObject);
    }

    @Override
    public JSONArray toJSONArray(Object obj) {
        com.tyyd.framework.dat.json.JSONArray jsonArray = new com.tyyd.framework.dat.json.JSONArray(obj);
        return new LtsJSONArray(jsonArray);
    }

    @Override
    public JSONArray parseArray(String json) {
        return new LtsJSONArray(new com.tyyd.framework.dat.json.JSONArray(json));
    }

    @Override
    public JSONObject parseObject(String json) {
        return new LtsJSONObject(new com.tyyd.framework.dat.json.JSONObject(json));
    }

    @Override
    public JSONObject newJSONObject() {
        return new LtsJSONObject(new com.tyyd.framework.dat.json.JSONObject());
    }

    @Override
    public JSONObject newJSONObject(Map<String, Object> map) {
        return new LtsJSONObject(new com.tyyd.framework.dat.json.JSONObject(map));
    }

    @Override
    public JSONObject newJSONObject(int initialCapacity) {
        return new LtsJSONObject(new com.tyyd.framework.dat.json.JSONObject(initialCapacity));
    }

    @Override
    public JSONArray newJSONArray() {
        return new LtsJSONArray(new com.tyyd.framework.dat.json.JSONArray());
    }

    @Override
    public JSONArray newJSONArray(List<Object> list) {
        return new LtsJSONArray(new com.tyyd.framework.dat.json.JSONArray(list));
    }

    @Override
    public JSONArray newJSONArray(int initialCapacity) {
        return new LtsJSONArray(new com.tyyd.framework.dat.json.JSONArray(initialCapacity));
    }
}

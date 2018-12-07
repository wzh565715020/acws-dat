package com.tyyd.framework.dat.management.web.support;

import org.springframework.util.StringUtils;

import com.tyyd.framework.dat.core.json.JSON;
import com.tyyd.framework.dat.core.json.TypeReference;

import java.beans.PropertyEditorSupport;
import java.util.HashMap;
import java.util.Map;

public class MapEditor extends PropertyEditorSupport {

    public MapEditor() {
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (!StringUtils.hasText(text)) {
            setValue(null);
        } else {
            setValue(JSON.parse(text, new TypeReference<HashMap<String, String>>(){}));
        }
    }

    @Override
    public String getAsText() {
        Map<?, ?> value = (Map<?, ?>) getValue();

        if (value == null) {
            return "";
        }
        return JSON.toJSONString(value);
    }
}

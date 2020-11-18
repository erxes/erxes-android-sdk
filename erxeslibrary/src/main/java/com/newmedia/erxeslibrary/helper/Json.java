package com.newmedia.erxeslibrary.helper;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Json {
    public boolean is_object;
    public List<Map> array;
    public Object object;

    public String getString(String name) {
        if (object != null && object instanceof Map && ((Map)object).get(name) != null) {
            return ((Map)object).get(name).toString();
        }
        return null;
    }

    public Boolean getBoolean(String name) {
        if (object != null && object instanceof Map && ((Map)object).get(name) != null) {
            return (Boolean) ((Map)object).get(name);
        }
        return false;
    }

    public boolean has(String name) {
        return object != null && object instanceof Map && ((Map)object).containsKey(name);
    }

    public Json(List<Map> array) {
        this.is_object = false;
        this.array = array;
    }

    public Json(Map object) {
        this.is_object = true;
        this.object = object;
    }

    public Json(Object object) {
        this.is_object = true;
        this.object = object;
    }

    @NonNull
    @Override
    public String toString() {
        if (object != null)
            return object.toString();
        else
            return array.toString();
    }
}

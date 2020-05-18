package com.newmedia.erxeslibrary.connection.helper;


import com.apollographql.apollo.api.CustomTypeAdapter;
import com.apollographql.apollo.api.CustomTypeValue;
import com.newmedia.erxeslibrary.helper.Json;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonCustomTypeAdapter implements CustomTypeAdapter<Json> {

    public Json decode(CustomTypeValue value) {
        String str = value.value.toString();
        if (str.length() > 0) {
            if (str.charAt(0) == '[') {
                List<Map> convertList = (List<Map>) value.value;
                return new Json(convertList);
            } else {
                Map a = (Map)(value.value);
                return new Json(a);
            }
        }
        return null;

    }

    public CustomTypeValue encode(Json value){
        if (value.is_object) {
            if (value.object == null) {
                value.object = new HashMap();
            }
            return new CustomTypeValue.GraphQLJsonObject(value.object);
        } else {
            if (value.array == null) {
                value.array = new ArrayList<>();
            }
            return new CustomTypeValue.GraphQLJsonList(Collections.singletonList(value.array));
        }
    }
}

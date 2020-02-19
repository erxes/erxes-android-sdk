package com.newmedia.erxeslibrary.connection.helper;


import com.apollographql.apollo.response.CustomTypeAdapter;
import com.apollographql.apollo.response.CustomTypeValue;
import com.newmedia.erxeslibrary.helper.Json;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class JsonCustomTypeAdapter implements CustomTypeAdapter<Json> {

    public Json decode(@NotNull CustomTypeValue value) {
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

    @NotNull
    public CustomTypeValue encode(@NotNull Json value){
        if(value.is_object)
            return new CustomTypeValue.GraphQLJsonObject(value.object);
        else {
            return new CustomTypeValue.GraphQLJsonList(Collections.singletonList(value.array));
        }
    }
}

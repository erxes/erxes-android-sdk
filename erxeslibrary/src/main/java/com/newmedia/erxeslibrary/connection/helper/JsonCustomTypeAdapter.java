package com.newmedia.erxeslibrary.connection.helper;


import com.apollographql.apollo.response.CustomTypeAdapter;
import com.apollographql.apollo.response.CustomTypeValue;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonCustomTypeAdapter implements CustomTypeAdapter<JSONObject> {

    public JsonCustomTypeAdapter() {

    }

    @Override
    public JSONObject decode(@NotNull CustomTypeValue value) {

        try {
            return new JSONObject( value.value.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;

    }

    @NotNull
    @Override
    public CustomTypeValue encode(@NotNull JSONObject value) {

        return new CustomTypeValue.GraphQLJsonString(value.toString());
    }
}

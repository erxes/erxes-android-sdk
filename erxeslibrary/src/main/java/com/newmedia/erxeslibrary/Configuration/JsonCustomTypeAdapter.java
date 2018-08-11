package com.newmedia.erxeslibrary.Configuration;

import android.util.Log;


import com.apollographql.apollo.response.CustomTypeAdapter;
import com.apollographql.apollo.response.CustomTypeValue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.Nonnull;

public class JsonCustomTypeAdapter implements CustomTypeAdapter<JSONObject> {

    public JsonCustomTypeAdapter() {

    }

    @Override
    public JSONObject decode(@Nonnull CustomTypeValue value) {

        try {
            return new JSONObject( value.value.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;

    }

    @Nonnull
    @Override
    public CustomTypeValue encode(@Nonnull JSONObject value) {
//        return new CustomTypeValue.GraphQLString(value);
//        return CustomTypeValue.fromRawValue(value);
        return new CustomTypeValue.GraphQLJsonString(value.toString());
    }
}

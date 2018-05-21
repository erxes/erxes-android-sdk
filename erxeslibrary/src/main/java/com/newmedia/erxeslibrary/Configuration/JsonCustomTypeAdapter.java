package com.newmedia.erxeslibrary.Configuration;

import com.apollographql.apollo.CustomTypeAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.Nonnull;

public class JsonCustomTypeAdapter implements CustomTypeAdapter<JSONObject> {

    public JsonCustomTypeAdapter() {

    }

    @Override
    public JSONObject decode(@Nonnull String value) {
        try {
            return new JSONObject(value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new JSONObject();

    }

    @Nonnull
    @Override
    public String encode(@Nonnull JSONObject value) {
        return value.toString();
    }
}

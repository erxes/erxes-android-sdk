package com.newmedia.erxeslibrary.Configuration;

import android.util.Log;


import com.apollographql.apollo.response.CustomTypeAdapter;
import com.apollographql.apollo.response.CustomTypeValue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.Nonnull;

public class JsonCustomTypeAdapter implements CustomTypeAdapter<String> {

    public JsonCustomTypeAdapter() {

    }

    @Override
    public String decode(@Nonnull CustomTypeValue value) {
        return value.value.toString();
    }

    @Nonnull
    @Override
    public CustomTypeValue encode(@Nonnull String value) {
        return new CustomTypeValue.GraphQLString(value);
    }
}

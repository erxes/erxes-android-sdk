package com.newmedia.erxeslibrary;

import org.json.JSONException;
import org.json.JSONObject;

public class FIleInfo {
    public String filepath,size,name,type,attachments;
    JSONObject get(){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type",type);
            jsonObject.put("size",size);
            jsonObject.put("name",name);
            jsonObject.put("url",filepath);
            return jsonObject;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}

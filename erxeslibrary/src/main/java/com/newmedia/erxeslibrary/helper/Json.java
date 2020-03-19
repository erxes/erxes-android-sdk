package com.newmedia.erxeslibrary.helper;

import android.support.annotation.NonNull;

import java.util.List;
import java.util.Map;

public class Json {
    public boolean is_object;
    public List<Map> array;
    public Map object;

    public String getString(String name) {
        if (object != null && object.get(name) != null) {
            return object.get(name).toString();
        }
        return null;
    }

    public boolean has(String name) {
        return object != null && object.containsKey(name);
    }

    public Json(List<Map> array) {
        this.is_object = false;
        this.array = array;
    }

    public Json(Map object) {
        this.is_object = true;
        this.object = object;
    }

//    private ArrayList convert_list(String str) {
//        JSONArray array = null;
//        try {
//            array = new JSONArray(str);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        ArrayList<Object> arrayList = new ArrayList<>();
//        for (int i = 0; i < array.length(); i++) {
//            try {
//                Object a = array.get(i);
//                if (a instanceof Integer) {
//                    arrayList.add(array.getInt(i));
//                } else if (a instanceof Double) {
//                    arrayList.add(array.getDouble(i));
//                } else if (a instanceof Boolean) {
//                    arrayList.add(array.getBoolean(i));
//                } else if (a instanceof String) {
//                    arrayList.add(array.getString(i));
//                } else if (a instanceof JSONObject) {
//                    arrayList.add(convert_object(array.getString(i)));
//                } else if (a instanceof JSONArray) {
//                    arrayList.add(convert_list(array.getString(i)));
//                }
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//        return arrayList;
//    }

//    private Map convert_object(String str) {
//        JSONObject object = null;
//        try {
//            object = new JSONObject(str);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        if (object != null) {
//            Map map = new HashMap();
//            Iterator<String> it = object.keys();
//            while (it.hasNext()) {
//                String key = it.next();
//                try {
//                    Object a = object.get(key);
//                    if (a instanceof Integer) {
//                        map.put(key, object.getInt(key));
//                    } else if (a instanceof Double) {
//                        map.put(key, object.getDouble(key));
//                    } else if (a instanceof Boolean) {
//                        map.put(key, object.getBoolean(key));
//                    } else if (a instanceof String) {
//                        map.put(key, object.getString(key));
//                    } else if (a instanceof JSONObject) {
//                        map.put(key, convert_object(object.getString(key)));
//                    } else if (a instanceof JSONArray) {
//                        map.put(key, convert_list(object.getString(key)));
//                    }
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//            return map;
//        }
//
//        return null;
//    }

//    List convert_list() {
//        return convert_list(array.toString());
//    }
//
//    Map convert_object() {
//        return convert_object(object.toString());
//    }

    @NonNull
    @Override
    public String toString() {
        if (object != null)
            return object.toString();
        else
            return array.toString();
    }
}

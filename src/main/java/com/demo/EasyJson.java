package com.demo;

import org.json.JSONException;
import org.json.JSONObject;

public class EasyJson extends JSONObject {
    public EasyJson(String json) {
        super(json);
    }

    public <T> T getT(String key) throws JSONException {
        return (T) get(key);
    }

    public Object get(String key) throws JSONException {
        if (key == null) {
            throw new JSONException("Null key.");
        }

        Object object = this.opt(key);
        if (object == null) {
            if (key.contains(".")) {
                object = this.getWithDotNotation(key);
            } else {
                return null;
            }
        }
        return object;
    }


    private Object getWithDotNotation(String key) throws JSONException {

        if (key.contains(".")) {
            int indexOfDot = key.indexOf(".");
            int indexOfDotSlash = key.indexOf("\\.");
            if (indexOfDot == indexOfDotSlash + 1) {
                //TODO: for common case
                return super.get(key.replace("\\", ""));
            }
            String subKey = key.substring(0, indexOfDot);
            JSONObject jsonObject = (JSONObject) this.get(subKey);
            if (jsonObject == null) {
                throw new JSONException(subKey + " is null");
            }
            try {
                return new EasyJson(jsonObject.toString()).getWithDotNotation(key.substring(indexOfDot + 1));
            } catch (JSONException e) {
                throw new JSONException(subKey + "." + e.getMessage());
            }
        } else {
            return this.get(key);
        }
    }
}

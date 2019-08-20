package com.newmedia.erxeslibrary.model;

import org.json.JSONException;
import org.json.JSONObject;

public class Geo {
    private String id, network, countryCode, region, city, zipCode, latitude, longitude, countryName;

    public static Geo convert(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            Geo geo = new Geo();
            if (jsonObject.has("id"))
                geo.setId(jsonObject.getString("id"));
            if (jsonObject.has("network"))
                geo.setNetwork(jsonObject.getString("network"));
            if (jsonObject.has("countryCode"))
                geo.setCountryCode(jsonObject.getString("countryCode"));
            if (jsonObject.has("region"))
                geo.setRegion(jsonObject.getString("region"));
            if (jsonObject.has("city"))
                geo.setCity(jsonObject.getString("city"));
            if (jsonObject.has("zipCode"))
                geo.setZipCode(jsonObject.getString("zipCode"));
            if (jsonObject.has("latitude"))
                geo.setLatitude(jsonObject.getString("latitude"));
            if (jsonObject.has("longitude"))
                geo.setLongitude(jsonObject.getString("longitude"));
            if (jsonObject.has("countryName"))
                geo.setCountryName(jsonObject.getString("countryName"));
            return geo;
        } catch (JSONException e) {
            e.printStackTrace();
            return new Geo();
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }
}

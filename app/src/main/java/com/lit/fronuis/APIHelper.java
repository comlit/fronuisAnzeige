package com.lit.fronuis;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONException;
import org.json.JSONObject;

public class APIHelper
{

    OkHttpClient client;

    public APIHelper()
    {
        client = new OkHttpClient();
    }
    public Map<String, String> getData()
    {
        Request request = new Request.Builder()
                .url("http://192.168.178.58/status/powerflow")
                .build();
        try (Response response = client.newCall(request).execute()) {
            JSONObject jo = new JSONObject(response.body().string());
            Map<String, String> dict = new HashMap<String, String>();
            dict.put("cons",jo.getJSONObject("site").get("P_Load").toString());
            dict.put("prod",jo.getJSONObject("site").get("P_PV").toString());
            dict.put("grid",jo.getJSONObject("site").get("P_Grid").toString());
            dict.put("batt",jo.getJSONArray("inverters").getJSONObject(0).get("SOC").toString());
            dict.put("battuse",jo.getJSONObject("site").get("P_Akku").toString());
            return dict;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        APIHelper api = new APIHelper();
        Map j = api.getData();

        System.out.println(j.toString());
    }
}


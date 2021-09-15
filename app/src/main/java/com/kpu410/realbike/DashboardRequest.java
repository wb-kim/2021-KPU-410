package com.kpu410.realbike;
import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class DashboardRequest extends StringRequest {

    //서버 URL 설정(php 파일 연동)
    final static private String URL = "http://bashw80.ivyro.net/dashboard.php";
    private Map<String, String> map;
    //private Map<String, String>parameters;

    public DashboardRequest(String userID, Response.Listener<String> listener) {
        super(Method.POST, URL, listener, null);

        map = new HashMap<>();
        map.put("userID",userID);
    }

    @Override
    protected Map<String, String>getParams() throws AuthFailureError {
        return map;
    }
}
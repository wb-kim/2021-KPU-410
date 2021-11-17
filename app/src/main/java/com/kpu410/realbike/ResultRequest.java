package com.kpu410.realbike;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class ResultRequest extends StringRequest {

    //서버 URL 설정(php 파일 연동)
    final static private String URL = "http://bashw80.ivyro.net/result.php";
    private Map<String, String> map;
    //private Map<String, String>parameters;

    public ResultRequest(String userID,String startLoc,String finishLoc,String moveLength,String moveTime,Response.Listener<String> listener){
        super(Method.POST, URL, listener, null);

        //Log.i("userID ", userID);
        //Log.i("start ", startLoc);
        //Log.i("finish ", finishLoc);
        //Log.i("time ", moveTime);
        //Log.i("length ", moveLength);
        map = new HashMap<>();
        map.put("userID",userID);
        map.put("start",startLoc);
        map.put("finish",finishLoc);
        map.put("time",moveTime);
        map.put("length",moveLength);

    }

    @Override
    protected Map<String, String>getParams() throws AuthFailureError {
        return map;
    }
}
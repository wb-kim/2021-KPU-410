package com.kpu410.realbike;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class InfoActivity extends AppCompatActivity {

    private TextView infoID;
    private TextView infoName;
    private TextView infoEmail;
    private TextView infoDistance;
    private TextView infoTime;

    private String userID;
    private String userPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        Intent myPageIntent = getIntent();
        userID = myPageIntent.getStringExtra("userID");
        userPass = myPageIntent.getStringExtra("userPass");

        infoID = findViewById(R.id.infoID);
        infoName = findViewById(R.id.infoName);
        infoEmail = findViewById(R.id.infoEmail);
        infoDistance = findViewById(R.id.infoDistance);
        infoTime = findViewById(R.id.infoTime);

        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    if (response.startsWith("ï»¿")){
                        response = response.substring(3);
                    }
                    JSONObject jsonObject = new JSONObject(response);
                    boolean success = jsonObject.getBoolean("success");
                    if (success) {
                        infoID.setText("접속중인 ID : ".concat(jsonObject.getString("userID")));
                        infoName.setText("닉네임 : ".concat(jsonObject.getString("userName")));
                        infoEmail.setText("나이 : ".concat(jsonObject.getString("userAge")));
                        infoDistance.setText("총 주행 거리 : ".concat(jsonObject.getString("total_distance")).concat("km"));
                        infoTime.setText("총 주행 시간 : ".concat(setTime(Integer.parseInt(jsonObject.getString("total_time")))));
                    } else {
                        Toast.makeText(getApplicationContext(), "정보 로드에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        InfoRequest infoRequest = new InfoRequest( userID, responseListener );
        RequestQueue queue = Volley.newRequestQueue( InfoActivity.this );
        queue.add( infoRequest );

    }

    private String setTime(int time) {
        int hour = time / 3600;
        int minute = (time - hour * 3600) / 60;
        int second = time % 60;
        return String.format("%02d", hour).concat(":").concat(String.format("%02d", minute)).concat(":").concat(String.format("%02d", second));
    }
}
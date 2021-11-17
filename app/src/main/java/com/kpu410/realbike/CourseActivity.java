package com.kpu410.realbike;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class CourseActivity extends AppCompatActivity {

    private TextView courseStart;
    private TextView courseDate;
    private TextView courseDistance;
    private TextView courseTime;

    private String userID;
    private String userPass;
    private String course_num;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course);

        Intent dashboardIntent = getIntent();
        userID = dashboardIntent.getStringExtra("userID");
        userPass = dashboardIntent.getStringExtra("userPass");
        course_num = dashboardIntent.getStringExtra("course_num");

        Log.i("course_num : ", course_num);
        courseStart = findViewById(R.id.courseStart);
        courseDate = findViewById(R.id.courseDate);
        courseDistance = findViewById(R.id.courseDistance);
        courseTime = findViewById(R.id.courseTime);

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
                        courseStart.setText("출발지 : ".concat(jsonObject.getString("start")));
                        courseDate.setText("주행일 : ".concat(jsonObject.getString("date")));
                        courseDistance.setText("주행거리 : ".concat(jsonObject.getString("length")).concat("km"));
                        courseTime.setText("주행시간 : ".concat(setTime(Integer.parseInt(jsonObject.getString("time")))));
                    } else {
                        Toast.makeText(getApplicationContext(), "정보 로드에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        CourseRequest courseRequest = new CourseRequest( course_num, responseListener );
        RequestQueue queue = Volley.newRequestQueue( CourseActivity.this );
        queue.add( courseRequest );
    }

    private String setTime(int time) {
        int hour = time / 3600;
        int minute = (time - hour * 3600) / 60;
        int second = time % 60;
        return String.format("%02d", hour).concat(":").concat(String.format("%02d", minute)).concat(":").concat(String.format("%02d", second));
    }
}
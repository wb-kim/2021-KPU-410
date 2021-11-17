package com.kpu410.realbike;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.Dash;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity {

    private TextView lengthDashboard, timeDashboard;
    private ListView listDashboard;
    private String userID;
    private String userPass;
    private ArrayList<HashMap<String, String>> list;
    private HashMap<String, String> item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        Intent myPageIntent = getIntent();
        userID = myPageIntent.getStringExtra("userID");
        userPass = myPageIntent.getStringExtra("userPass");

        listDashboard = findViewById(R.id.listDashboard);
        lengthDashboard = findViewById(R.id.lengthDashboard);
        timeDashboard = findViewById(R.id.timeDashboard);

        list = new ArrayList<HashMap<String, String>>();

        //List<String> list = new ArrayList<>();
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
                        JSONArray legArray = jsonObject.getJSONArray("0");
                        for (int i = 0; i < legArray.length(); i++) {
                            JSONObject legObject = legArray.getJSONObject(i);
                            item = new HashMap<String, String>();
                            item.put("course_num", legObject.getString("course_num"));
                            item.put("date", legObject.getString("date"));
                            item.put("start", legObject.getString("start"));
                            //Log.i("asdf : ", legObject.getString("course_num"));
                            list.add(item);
                            Log.i("in List : ", list.get(i).toString());
                        }
                        String total_time = setTime(Integer.parseInt(jsonObject.getString("total_time")));
                        lengthDashboard.setText("거리 : ".concat(jsonObject.getString("total_distance")).concat("km"));
                        timeDashboard.setText("시간 : ".concat(total_time));

                        SimpleAdapter listAdapter = new SimpleAdapter(getApplicationContext(), list, android.R.layout.simple_list_item_2,
                                new String[]{"date", "start"}, new int[]{android.R.id.text1, android.R.id.text2});

                        //ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);

                        listDashboard.setAdapter(listAdapter);

                        listDashboard.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                Intent intent = new Intent(getApplicationContext(), CourseActivity.class);
                                intent.putExtra("userID", userID);
                                intent.putExtra("userPass", userPass);
                                intent.putExtra("course_num", list.get(position).get("course_num"));
                                startActivity(intent);
                            }
                        });
                    } else {
                        Toast.makeText(getApplicationContext(), "정보 로드에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        DashboardRequest dashboardRequest = new DashboardRequest( userID, responseListener );
        RequestQueue queue = Volley.newRequestQueue( DashboardActivity.this );
        queue.add( dashboardRequest );

    }

    private String setTime(int time) {
        int hour = time / 3600;
        int minute = (time - hour * 3600) / 60;
        int second = time % 60;
        return String.format("%02d", hour).concat(":").concat(String.format("%02d", minute)).concat(":").concat(String.format("%02d", second));
    }
}
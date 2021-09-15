package com.kpu410.realbike;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity {

    private ListView listDashboard;
    private String userID;
    private String userPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        Intent myPageIntent = getIntent();
        userID = myPageIntent.getStringExtra("userID");
        userPass = myPageIntent.getStringExtra("userPass");

        listDashboard = findViewById(R.id.listDashboard);

        ArrayList<HashMap<String, String>> list = new ArrayList<>();

        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        boolean success = jsonObject.getBoolean("success");
                        if (success) {
                            HashMap<String, String> item = new HashMap<>();
                            item.put("course_num", jsonObject.getString("course_num"));
                            item.put("date", jsonObject.getString("date"));
                            item.put("start", jsonObject.getString("start"));

                            list.add(item);
                        } else {
                            Toast.makeText(getApplicationContext(), "정보 로드에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        DashboardRequest dashboardRequest = new DashboardRequest( userID, responseListener );
        RequestQueue queue = Volley.newRequestQueue( DashboardActivity.this );
        queue.add( dashboardRequest );

        String[] from = {"date", "start"};

        int[] to = new int[] {android.R.id.text1, android.R.id.text2};

        SimpleAdapter adapter = new SimpleAdapter(this, list, android.R.layout.simple_list_item_2, from, to);

        listDashboard.setAdapter(adapter);

        listDashboard.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
                intent.putExtra("userID", userID);
                intent.putExtra("userPass", userPass);
                intent.putExtra("course_num", list.get(position).get("course_num"));
                startActivity(intent);
            }
        });
    }
}
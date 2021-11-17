package com.kpu410.realbike;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.text.SimpleDateFormat;

public class ResultActivity extends AppCompatActivity {
    private String moveTime;
    private String moveLength;
    private String userID;
    private String userPass;
    private String startLoc;
    private String finishLoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        Intent loginIntent = getIntent();
        userID = loginIntent.getStringExtra("userID");
        userPass = loginIntent.getStringExtra("userPass");
        Intent mapIntent = getIntent();
        moveLength = mapIntent.getStringExtra("moveLength");
        moveTime = mapIntent.getStringExtra("moveTime");
        Intent searchIntent = getIntent();
        //startLoc = searchIntent.getStringExtra("startLoc");
        startLoc = "New York";
        //finishLoc = searchIntent.getStringExtra("finishLoc");
        finishLoc = "New York";
        TextView resultLength = findViewById(R.id.resultLength);
        resultLength.setText((moveLength).concat("km"));

        TextView resultTime = findViewById(R.id.resultTime);
        resultTime.setText(setTime(Integer.parseInt(moveTime)));
        Button btnresult = findViewById(R.id.btnresult);
        btnresult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean success = jsonObject.getBoolean("success");
                            if (success) { // 회원등록에 성공한 경우
                                Toast.makeText(getApplicationContext(),"결과 정보 등록이 완료되었습니다.",Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(ResultActivity.this, MyPageActivity.class);
                                intent.putExtra("userID", userID);
                                intent.putExtra("userPass", userPass);
                                startActivity(intent);
                            } else { // 회원등록에 실패한 경우
                                Toast.makeText(getApplicationContext(),"결과 정보 등록이 실패하였습니다.",Toast.LENGTH_SHORT).show();
                                return;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                };



                ResultRequest resultRequest = new ResultRequest (userID, startLoc,finishLoc,moveLength,moveTime,responseListener);
                RequestQueue queue = Volley.newRequestQueue(ResultActivity.this);
                queue.add(resultRequest);
            }});

        Button btnMain = findViewById(R.id.btnMain);
        btnMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(mainIntent);
            }
        });
    }

    private String setTime(int time) {
        int hour = time / 3600;
        int minute = (time - hour * 3600) / 60;
        int second = time % 60;
        return String.format("%02d", hour).concat(":").concat(String.format("%02d", minute)).concat(":").concat(String.format("%02d", second));
    }
}
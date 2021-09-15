package com.kpu410.realbike;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MyPageActivity extends AppCompatActivity {

    private String userID;
    private String userPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);

        Intent mainIntent = getIntent();
        userID = mainIntent.getStringExtra("userID");
        userPass = mainIntent.getStringExtra("userPass");

        Button btnInfo = findViewById(R.id.btnInfo);
        btnInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), InfoActivity.class);
                intent.putExtra("userID", userID);
                intent.putExtra("userPass", userPass);
                startActivity(intent);
            }
        });

        Button btnDashboard = findViewById(R.id.btnDashboard);
        btnDashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
                intent.putExtra("userID", userID);
                intent.putExtra("userPass", userPass);
                startActivity(intent);
            }
        });
    }
}
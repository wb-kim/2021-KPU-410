package com.kpu410.realbike;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ResultActivity extends AppCompatActivity {
    private String moveTime;
    private String moveLength;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        Intent mapIntent = getIntent();

        moveLength = mapIntent.getStringExtra("moveLength");
        moveTime = mapIntent.getStringExtra("moveTime");


        TextView resultLength = findViewById(R.id.resultLength);
        resultLength.setText("총 주행 거리 : ".concat(moveLength).concat("km"));

        TextView resultTime = findViewById(R.id.resultTime);
        resultTime.setText("주행 시간 : ".concat(moveTime).concat("초"));

        Button btnMain = findViewById(R.id.btnMain);
        btnMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(mainIntent);
            }
        });
    }
}
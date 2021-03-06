// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.


package com.kpu410.realbike;

import com.google.android.gms.common.server.converter.StringToIntConverter;
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.SupportStreetViewPanoramaFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.StreetViewPanoramaOrientation;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

/**
 * This shows how to create a simple activity with streetview
 */
public class DriveActivity extends AppCompatActivity {
    private static final String API_KEY="AIzaSyAIgidUd3VceMR-6DnCFvrFNyySINLiVWo";
    private static final double CIRCLE = 4.11;
    private String strUrl = null;           // EditText + API URL

    private StreetViewPanorama streetViewPanorama;
    private List<LatLng> routeLatLng = new ArrayList();
    private int routeCount = 0;

    private int list_len = 0;
    private int route_len = 0;

    private int temp = 0;
    private int count = 0;
    private int[] feet;

    private BluetoothSPP bt;

    private String startLoc;
    private String finishLoc;

    private String moveLength;

    private double startTime;
    private double endTime;
    private int moveTime;

    private LatLng startLatLng;

    private String userID;
    private String userPass;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drive);


        Button btnFinishDrive = findViewById(R.id.btnFinishDrive);

        btnFinishDrive.setOnClickListener(this::btnFinishDrive);


        Intent searchIntent = getIntent();
        userID = searchIntent.getStringExtra("userID");
        userPass = searchIntent.getStringExtra("userPass");
        startLoc = searchIntent.getStringExtra("startLoc");
        finishLoc = searchIntent.getStringExtra("finishLoc");

        bt = new BluetoothSPP(this); //Initializing

        if (!bt.isBluetoothAvailable()) { //???????????? ?????? ??????
            Toast.makeText(getApplicationContext()
                    , "Bluetooth is not available"
                    , Toast.LENGTH_SHORT).show();
            finish();
        }

        bluetoothConnect();
        jsonPashing();


        Log.i("size", String.valueOf(routeLatLng.size()));


        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() { //????????? ??????
            TextView distance = findViewById(R.id.distance);

            int messageCheck = 0;

            public void onDataReceived(byte[] data, String message) {
                if (message.length() != 11 && messageCheck == 0) {
                    Log.i("message from arduino", message);
                } else {
                    messageCheck = 1;
                    String[] array = message.split(",");
                    Log.i("message from arduino", message);
                    moveLength = array[2];
                    String total = "?????? : ".concat((array[1].concat("km/h, ?????? ?????? : ")).concat(array[2].concat("km")));
                    distance.setText(total);
                    temp = 0;

                    String checkArray = array[0];
                    int check = Integer.parseInt(checkArray);
                    if (check == 0) {
                        count++;
                        if (count == 10) {
                            temp = 1;
                            count = 0;
                            routeCount++;
                        }
                    }
                    //Log.i("?????? ??????", check+","+temp+","+routeLat.length+","+routeLng.length);
                    Log.i("?????? ??????", route_len + " : " + routeLatLng.get(route_len).latitude + "," + routeLatLng.get(route_len).longitude);

                    if (temp == 1 && routeCount == 3) {
                        if (route_len == routeLatLng.size() || route_len > routeLatLng.size()) {
                            streetViewPanorama.setPosition(routeLatLng.get(routeLatLng.size() - 1));
                            finishDrive();
                        } else {
                            route_len = route_len + 3;
                            streetViewPanorama.setPosition(routeLatLng.get(route_len));
                            routeCount = 0;
                        }
                    }
                }
            }
        });

        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() { //???????????? ???
            public void onDeviceConnected(String name, String address) {
                Toast.makeText(getApplicationContext()
                        , "Connected to " + name + "\n" + address
                        , Toast.LENGTH_SHORT).show();
                startTime = System.currentTimeMillis();
            }

            public void onDeviceDisconnected() { //????????????
                Toast.makeText(getApplicationContext()
                        , "Connection lost", Toast.LENGTH_SHORT).show();
            }

            public void onDeviceConnectionFailed() { //????????????
                Toast.makeText(getApplicationContext()
                        , "Unable to connect", Toast.LENGTH_SHORT).show();
                finish();
            }
        });


        SupportStreetViewPanoramaFragment streetViewPanoramaFragment =
                (SupportStreetViewPanoramaFragment)
                        getSupportFragmentManager().findFragmentById(R.id.streetviewpanorama);
        streetViewPanoramaFragment.getStreetViewPanoramaAsync(
                new OnStreetViewPanoramaReadyCallback() {
                    @Override
                    public void onStreetViewPanoramaReady(StreetViewPanorama panorama) {
                        streetViewPanorama = panorama;
                        if (savedInstanceState == null) {
                            panorama.setPosition(routeLatLng.get(0));
                        }
                    }

                });



    }

    public void finishDrive() {
        AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(DriveActivity.this);
        dlgBuilder.setTitle("?????? ??????");
        dlgBuilder.setMessage("?????????????????? ????????? ?????????????????????.");
        dlgBuilder.setPositiveButton("?????? ????????????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onDestroy();
                Intent resultIntent = new Intent(getApplicationContext(), ResultActivity.class);
                resultIntent.putExtra("userID", userID);
                resultIntent.putExtra("userPass", userPass);
                resultIntent.putExtra("moveLength", moveLength);
                resultIntent.putExtra("moveTime", moveTime);
                startActivity(resultIntent);
            }
        });
        AlertDialog dlg = dlgBuilder.create();
        dlg.show();
    }

    public void btnFinishDrive(View view) {
        AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(DriveActivity.this);
        dlgBuilder.setTitle("?????? ??????");
        dlgBuilder.setMessage("????????? ?????????????????????.");
        endTime = System.currentTimeMillis();
        moveTime = (int) ((endTime - startTime) / 1000);
        dlgBuilder.setPositiveButton("?????? ????????????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onDestroy();
                Intent resultIntent = new Intent(getApplicationContext(), ResultActivity.class);
                resultIntent.putExtra("userID", userID);
                resultIntent.putExtra("userPass", userPass);
                resultIntent.putExtra("moveLength", moveLength);
                resultIntent.putExtra("moveTime", String.valueOf(moveTime));
                startActivity(resultIntent);
            }
        });
        AlertDialog dlg = dlgBuilder.create();
        dlg.show();
    }

    public void bluetoothConnect() {
        if (bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
            bt.disconnect();
        } else {
            Intent intent = new Intent(getApplicationContext(), DeviceList.class);
            startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
        }
    }

    public class Task extends AsyncTask<String, Void, String> {

        private String str, receiveMsg;

        @Override
        protected String doInBackground(String... strings) {
            URL url = null;
            try {
                url = new URL(strUrl);
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                if (conn.getResponseCode() == conn.HTTP_OK) {
                    InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "UTF-8");
                    BufferedReader reader = new BufferedReader(tmp);
                    StringBuffer buffer = new StringBuffer();
                    while ((str = reader.readLine()) != null) {
                        buffer.append(str);
                    }
                    receiveMsg = buffer.toString();     // JSON to String

                    reader.close();
                } else {
                    Log.e("?????? ??????", "ERRORCODE" + conn.getResponseCode() + "/ URL : " + strUrl);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return receiveMsg;
        }
    }

    public void jsonPashing() {
        strUrl = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=" + startLoc + "&destination=" + finishLoc + "&mode=bicycling&departure_time=now" +
                "&alternatives=true&language=Korean&key=" + API_KEY;
        Log.i("url", strUrl);
        String resultText = "?????? ??????";

        try {
            resultText = new Task().execute().get();

            JSONObject jsonObject = new JSONObject(resultText);
            String routes = jsonObject.getString("routes");
            JSONArray routesArray = new JSONArray(routes);
            JSONObject subJsonObject = routesArray.getJSONObject(0);

            String legs = subJsonObject.getString("legs");
            JSONArray legArray = new JSONArray(legs);
            JSONObject legJsonObject = legArray.getJSONObject(0);

            String startL = legJsonObject.getString("start_location");
            JSONObject startLObject = new JSONObject(startL);
            double sLat = startLObject.getDouble("lat");
            double sLng = startLObject.getDouble("lng");

            startLatLng = new LatLng(sLat, sLng);

            routeLatLng.add(0, startLatLng);

            String steps = legJsonObject.getString("steps");
            JSONArray stepsArray = new JSONArray(steps);
            list_len = stepsArray.length();

            feet = new int[list_len];

            for (int i = 0; i < list_len; i++) {
                JSONObject stepsObject = stepsArray.getJSONObject(i);

                String endLoc = stepsObject.getString("end_location");
                JSONObject endObject = new JSONObject(endLoc);
                double eLat = endObject.getDouble("lat");
                double eLng = endObject.getDouble("lng");

                String distance = stepsObject.getString("distance");
                JSONObject distanceObject = new JSONObject(distance);
                String[] array = distanceObject.getString("text").split(" ");

                int distanceT;
                if (array[1].equals("mi")) {
                    distanceT = (int)(Double.parseDouble(array[0]) * 5280);
                } else {
                    distanceT = Integer.parseInt(array[0]);
                }

                feet[i] = (int)(distanceT / CIRCLE);
                Log.i("feet size", String.valueOf(feet[i]));

                LatLng lastLatLng = routeLatLng.get(routeLatLng.size() - 1);
                double diffLat = (eLat - lastLatLng.latitude) / feet[i];
                double diffLng = (eLng - lastLatLng.longitude) / feet[i];
                Log.i("diffLatLng", String.valueOf(diffLat) + "," + String.valueOf(diffLng));


                double addLat = lastLatLng.latitude;
                double addLng = lastLatLng.longitude;

                for (int j = 0; j < (feet[i] + 1); j++) {
                    addLat += diffLat;
                    addLng += diffLng;
                    routeLatLng.add(new LatLng(addLat, addLng));
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }
    public void onDestroy() {
        super.onDestroy();
        bt.stopService(); //???????????? ??????
    }

    public void onStart() {
        super.onStart();
        if (!bt.isBluetoothEnabled()) { //
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        } else {
            if (!bt.isServiceAvailable()) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER); //DEVICE_ANDROID??? ??????????????? ?????? ??????
                setup();
            }
        }
    }

    public void setup() {
        bt.send("Reset", true);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if (resultCode == Activity.RESULT_OK)
                bt.connect(data);
        } else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);
                setup();
            } else {
                Toast.makeText(getApplicationContext()
                        , "Bluetooth was not enabled."
                        , Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
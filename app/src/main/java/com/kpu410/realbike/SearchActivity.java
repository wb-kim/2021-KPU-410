package com.kpu410.realbike;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

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
import java.util.List;
import java.util.concurrent.ExecutionException;



public class SearchActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String API_KEY="AIzaSyAIgidUd3VceMR-6DnCFvrFNyySINLiVWo";
    private String strUrl = null;           // EditText + API URL
    private String getOverview = null;      // 폴리라인 보여주기 위한 변수
    private static int AUTOCOMPLETE_REQUEST_CODE = 1;

    private GoogleMap mMap;

    private EditText startLoc;
    private EditText finishLoc;

    private int autoSearch = 0;

    private String userID;
    private String userPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Intent mainIntent = getIntent();
        userID = mainIntent.getStringExtra("userID");
        userPass = mainIntent.getStringExtra("userPass");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Places.initialize(getApplicationContext(), API_KEY);

        startLoc = findViewById(R.id.startLoc);
        finishLoc = findViewById(R.id.finishLoc);


        startLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAutocompleteActivity();
                autoSearch = 1;
            }
        });

        finishLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAutocompleteActivity();
                autoSearch = 0;
            }
        });

        Button btnSearch = findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(this::searchClick);

        Button btnDrive = findViewById(R.id.btnDrive);
        btnDrive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mapIntent = new Intent(getApplicationContext(), DriveActivity.class);
                mapIntent.putExtra("userID", userID);
                mapIntent.putExtra("userPass", userPass);
                mapIntent.putExtra("startLoc", startLoc.getText().toString());
                mapIntent.putExtra("finishLoc", finishLoc.getText().toString());
                startActivity(mapIntent);
            }
        });
    }

    private void startAutocompleteActivity() {
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME);
        // Start the autocomplete intent.
        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY, fields)
                .build(this);
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                if (autoSearch == 1) {
                    startLoc.setText(place.getName());
                } else {
                    finishLoc.setText(place.getName());
                }
                Log.i("AutoPlace", "Place: " + place.getName() + ", " + place.getId());
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i("AutoPlace", status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Newyork and move the camera
        //LatLng newyork = new LatLng(40.730610, 	-73.935242);
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newyork, 10));
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
                    Log.e("통신 오류", "ERRORCODE" + conn.getResponseCode() + "/ URL : " + strUrl);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return receiveMsg;
        }
    }

    public static ArrayList<LatLng> decodePolyPoints(String encodedPath) {
        int len = encodedPath.length();

        final ArrayList<LatLng> path = new ArrayList<LatLng>();
        int index = 0;
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int result = 1;
            int shift = 0;
            int b;
            do {
                b = encodedPath.charAt(index++) - 63 - 1;
                result += b << shift;
                shift += 5;
            } while (b >= 0x1f);
            lat += (result & 1) != 0 ? ~(result >> 1) : (result >> 1);

            result = 1;
            shift = 0;
            do {
                b = encodedPath.charAt(index++) - 63 - 1;
                result += b << shift;
                shift += 5;
            } while (b >= 0x1f);
            lng += (result & 1) != 0 ? ~(result >> 1) : (result >> 1);

            path.add(new LatLng(lat * 1e-5, lng * 1e-5));
        }
        return path;
    }

    public void searchClick(View view) {
        String start = startLoc.getText().toString();
        String finish = finishLoc.getText().toString();

        strUrl = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=" + start + "&destination=" + finish + "&mode=bicycling&departure_time=now" +
                "&alternatives=true&language=Korean&key=" + API_KEY;
        // origin = 출발지, destination = 도착지, mode = 자전거도로, departure_time = 출발 시간

        String resultText = "값이 없음";

        try {
            resultText = new Task().execute().get();

            JSONObject jsonObject = new JSONObject(resultText);
            String routes = jsonObject.getString("routes");
            JSONArray routesArray = new JSONArray(routes);

            JSONObject preferredObject = routesArray.getJSONObject(0);
            String singleRoute = preferredObject.getString("overview_polyline");
            JSONObject pointsObject = new JSONObject(singleRoute);
            String points = pointsObject.getString("points");

            getOverview = points;

            if (getOverview != null) {
                ArrayList<LatLng> entirePath = decodePolyPoints(getOverview);

                for (int i = 0; i < entirePath.size(); i++) {
                    if (i == 0) {
                        mMap.addMarker(new MarkerOptions().position(entirePath.get(i)).title("출발"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(entirePath.get(i), 14));
                    } else if (i >= entirePath.size() - 1) {
                        mMap.addMarker(new MarkerOptions().position(entirePath.get(i)).title("도착"));
                    }
                }

                Polyline line = null;
                if (line == null) {
                    line = mMap.addPolyline(new PolylineOptions()
                            .color(Color.rgb(0, 153, 255))
                            .geodesic(true)
                            .addAll(entirePath));
                } else {
                    line.remove();
                    line = mMap.addPolyline(new PolylineOptions()
                            .color(Color.rgb(0, 153, 255))
                            .geodesic(true)
                            .addAll(entirePath));
                }
            }
            onMapReady(mMap);


        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

}
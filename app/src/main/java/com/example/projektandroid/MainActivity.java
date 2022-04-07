package com.example.projektandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private SensorManager mSensorManager;
    private float mAccel;
    private float mAccelCurrent;
    private float mAccelLast;
    private RequestQueue mQueue;
    public ArrayList<NBP> currencyList = new ArrayList<>();
    Random rnd = new Random();
    int position;
    EditText plnto;
    EditText buycurrency;
    EditText sellcurrency;
    Button button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        button = findViewById(R.id.button_parse);
        plnto = findViewById(R.id.plnto);
        buycurrency = findViewById(R.id.buycurrency);
        sellcurrency = findViewById(R.id.sellcurrency);
        mQueue = Volley.newRequestQueue(this);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Objects.requireNonNull(mSensorManager).registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        mAccel = 10f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;

        plnto.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {

                if (s.toString().equals("")) {
                    buycurrency.setText("");
                    sellcurrency.setText("");
                } else {
                    try {
                        Double value = Double.parseDouble(s.toString());
                        double buy, sell;
                        buy = value * currencyList.get(position).bid;
                        sell = value * currencyList.get(position).ask;
                        buycurrency.setText(String.format("%.2f", buy));
                        sellcurrency.setText(String.format("%.2f", sell));
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(final CharSequence s, int start, int before, int count){

            }

        });


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                intent.putExtra("code" , currencyList.get(position).code);
                startActivity(intent);
            }
        });
    }

    private final SensorEventListener mSensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            TextView mTextViewResult = findViewById(R.id.text_view_result);
            TextView text = findViewById(R.id.start);
            TextView plninfo =  findViewById(R.id.plninfo);
            TextView buyinfo =  findViewById(R.id.buyinfo);
            TextView sellinfo =  findViewById(R.id.sellinfo);

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            mAccelLast = mAccelCurrent;
            mAccelCurrent = (float) Math.sqrt((double) (x * x + y * y + z * z));
            float delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel * 0.9f + delta;
            if (mAccel > 26) {
                if(isOnline()) {
                    try {

                        position = rnd.nextInt(currencyList.size() - 1);
                        mTextViewResult.setText("Waluta: " + currencyList.get(position).currency + "\n"
                                +"Cena kupna: " + currencyList.get(position).bid + "zł\n"
                                +"Cena sprzedaży: " + currencyList.get(position).ask + "zł");
                        text.setVisibility(View.GONE);
                        plnto.setVisibility(View.VISIBLE);
                        buycurrency.setVisibility(View.VISIBLE);
                        sellcurrency.setVisibility(View.VISIBLE);
                        plninfo.setVisibility(View.VISIBLE);
                        buyinfo.setVisibility(View.VISIBLE);
                        sellinfo.setVisibility(View.VISIBLE);
                        button.setVisibility(View.VISIBLE);

                    } catch (Exception e ){
                        Toast.makeText(getApplicationContext(), "Brak połączenia z internetem", Toast.LENGTH_SHORT).show();

                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Brak połączenia z internetem", Toast.LENGTH_SHORT).show();
                }
            }
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    @Override
    protected void onResume() {
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        super.onResume();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeReceiver, intentFilter);
    }
    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(mSensorListener);
        super.onPause();

        unregisterReceiver(networkChangeReceiver);
    }


    private void jsonParse(){

        String url = "https://api.nbp.pl/api/exchangerates/tables/C/?format=json";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null ,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            JSONObject jsonObject = response.getJSONObject(0);
                            JSONArray jsonArray = jsonObject.getJSONArray("rates");
                            for (int i=0;i<jsonArray.length(); i++){
                                JSONObject rate = jsonArray.getJSONObject(i);

                                String currency = rate.getString("currency").toUpperCase();
                                String code = rate.getString("code");
                                double bid = rate.getDouble("bid");
                                double ask = rate.getDouble("ask");

                                NBP nbp = new NBP(currency, code, bid, ask);
                                currencyList.add(nbp);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        mQueue.add(request);
    }

    public boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        }
        catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return false;
    }

    private BroadcastReceiver networkChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
                jsonParse();
        }
    };


}
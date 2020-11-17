package com.example.podometre;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private StringBuffer sensorText;
    //private TextView sensorList;
    private TextView stepsSinceReboot;
    private TextView stepsText;
    private List<Sensor> deviceSensors;
    private Button sendButton;

    private Boolean isSensorPresent;
    
    private Sensor sensorStepCounter;
    private Sensor sensorStepDetector;

    private int countSteps = 0;
    private StepsDBHelper stepsDBHelper;
    private ArrayList<DateStepsModel> stepCountList;
    private DateStepsModel currentDateStepEntry;

    private JSONArray jsonArrayToPost;

    private Context context;
    private ContentResolver contentResolver;


    Calendar calendar = Calendar.getInstance();
    String todayDate = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)) + "/"
            + String.valueOf(calendar.get(Calendar.MONTH) + 1)+ "/"
            + String.valueOf(calendar.get(Calendar.YEAR));



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //sensorList = (TextView)findViewById(R.id.sensorList);
        stepsSinceReboot = (TextView) findViewById(R.id.stepsSinceReboot);
        stepsText = (TextView) findViewById(R.id.stepsText);
        sendButton = (Button) findViewById(R.id.sendButton);

        sensorText = new StringBuffer();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        deviceSensors = sensorManager.getSensorList(Sensor.TYPE_ALL);

        if (sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null) {
            sensorStepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            isSensorPresent = true;
        } else {
            Toast.makeText(getApplicationContext(), "Pas de capteur \"STEP COUNTER\" présent dans l'appareil.", Toast.LENGTH_SHORT).show();
        }

        //listSensor();

        if (sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) != null) {
            sensorStepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
            isSensorPresent = true;
        } else {
            Toast.makeText(getApplicationContext(), "Pas de capteur \"STEP DETECTOR\" présent dans l'appareil.", Toast.LENGTH_SHORT).show();
        }

        if (isSensorPresent) {
            sensorManager.registerListener(this, sensorStepCounter, SensorManager.SENSOR_DELAY_NORMAL);
            sensorManager.registerListener(this, sensorStepDetector, SensorManager.SENSOR_DELAY_NORMAL);
        }

        getData(todayDate);

        jsonArrayToPost = putDataInJson(stepCountList);

        Log.d("TAG ///////////////////", String.valueOf(jsonArrayToPost));

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getData(todayDate);
                String url = "http://192.168.1.23/podometre/back/insert_step_entry.php";
                jsonArrayToPost = putDataInJson(stepCountList);
                /*StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Toast.makeText(MainActivity.this, response.trim(), Toast.LENGTH_LONG).show();
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_LONG).show();
                            }
                        }) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        params.put("step_count", String.valueOf(currentDateStepEntry.stepCount)); // Cast de int en String
                        params.put("date_creation", currentDateStepEntry.date);
                        return params;
                    }
                };
                RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
                requestQueue.add(stringRequest);*/

                JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.POST, url, jsonArrayToPost,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                try {
                                    /*JSONArray jsonArrayResponse = response.getJSONArray(0);
                                    JSONObject jsonObject = jsonArrayResponse.getJSONObject(0);
                                    String result = jsonObject.getString("success");
                                    Toast.makeText(MainActivity.this, result, Toast.LENGTH_LONG).show();*/
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                Toast.makeText(MainActivity.this, response.toString(), Toast.LENGTH_LONG).show();
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_LONG).show();
                            }
                        }

                );
                RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
                requestQueue.add(jsonArrayRequest);
            }
        });
    }

    /**
     * Pour que le programme reste actif même après l'avoir quitté
     */
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }


    public void getData(String todayDate) {
        stepsDBHelper = new StepsDBHelper(this);
        stepCountList = stepsDBHelper.getAllStepsEntries();
        currentDateStepEntry = stepsDBHelper.getCurrentDateStepEntry(todayDate);
        stepsText.setText(String.valueOf(currentDateStepEntry.date + "\nNombre de pas effectué :" + currentDateStepEntry.stepCount));

    }

    public JSONArray putDataInJson (ArrayList<DateStepsModel> stepEntryList) {
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < stepEntryList.size(); i++) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("step_count", String.valueOf(stepEntryList.get(i).stepCount));
                jsonObject.put("date_creation", stepEntryList.get(i).date);
                jsonArray.put(jsonObject);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        return jsonArray;
    }
    /*private void listSensor () {
        for (Sensor sensor :  deviceSensors) {
            String sensorName = sensor.getName();
            sensorText.append("" + sensorName + " \n");
            //Toast.makeText(getApplicationContext(), "" + sensorName, Toast.LENGTH_SHORT).show();
        }
        sensorList.setText(sensorText);
    }
    */

    private void countSteps(int step) {
        countSteps += step;
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case(Sensor.TYPE_STEP_COUNTER):
                stepsSinceReboot.setText(String.valueOf(event.values[0]) + "\n");
                break;
            case(Sensor.TYPE_STEP_DETECTOR):
                countSteps((int)event.values[0]);
                currentDateStepEntry = stepsDBHelper.getCurrentDateStepEntry(todayDate);
                stepsText.setText(String.valueOf(currentDateStepEntry.date + "\n" + currentDateStepEntry.stepCount));
                stepsDBHelper.createStepsEntry();
                break;
            default:
                break;
        }
    }
}
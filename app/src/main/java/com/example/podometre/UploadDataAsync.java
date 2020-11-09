package com.example.podometre;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class UploadDataAsync extends AsyncTask<Void, Void, Void> {
    private Context context;

    DateStepsModel stepsToPost;
    StepsDBHelper stepsDBHelper = new StepsDBHelper(context);


    Calendar calendar = Calendar.getInstance();
    String todayDate = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)) + "/"
            + String.valueOf(calendar.get(Calendar.MONTH) + 1)+ "/"
            + String.valueOf(calendar.get(Calendar.YEAR));

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected Void doInBackground(Void... params) {
        stepsToPost = stepsDBHelper.getCurrentDateStepEntry(todayDate);

        ContentValues values = new ContentValues();
        values.put("StepsCount", stepsToPost.stepCount);
        values.put("CreationDate", stepsToPost.date);


        try {
            /*JSONObject postData = new JSONObject();
            DateStepsModel currentDateStepEntry = stepsDBHelper.getCurrentDateStepEntry(todayDate);

            postData.put("stepscount", currentDateStepEntry.stepCount);
            postData.put("creationdate", currentDateStepEntry.date);*/

            URL url = new URL("http://192.168.1.23/podometre/sql_connect.php");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json; utf-8");
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setChunkedStreamingMode(0);

            String jsonInputString = "{'StepsCount': '139', 'CreationDate': '30/10/2020'}";


            OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());

            //BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
            try (OutputStream os = urlConnection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int code = urlConnection.getResponseCode();
            System.out.println(code);

            try(BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "utf-8"))){
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                System.out.println(response.toString());
            }


            //writer.write(values.toString());
            //writer.flush();

            /*int code = urlConnection.getResponseCode();
            if (code !=  201) {
                throw new IOException("Invalid response from server: " + code);
            }

            BufferedReader rd = new BufferedReader(new InputStreamReader(
                    urlConnection.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                Log.i("data", line);
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }/* finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }*/

        return null;
    }
}

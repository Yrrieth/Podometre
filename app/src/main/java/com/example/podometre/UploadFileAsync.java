package com.example.podometre;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Upload a SQLite file from android app to a PHP script
 */

public class UploadFileAsync extends AsyncTask<String, Void, String> {
    private Context context;
    private ContentResolver contentResolver;

    public UploadFileAsync(Context context, ContentResolver contentResolver) {
        this.context = context;
        this.contentResolver = contentResolver;
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            //String sourceFileUri = params[0];
            String sourceFileUri = Uri.fromFile(context.getDatabasePath(StepsDBHelper.DATABASE_NAME)).toString();
            //String sourceFileUri = context.getDatabasePath("StepsDataBase.db").getPath();
            //String fileName = params[1];


            HttpURLConnection connection = null;
            DataOutputStream dataOutputStream = null;
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;

            File sourceFile = context.getDatabasePath(StepsDBHelper.DATABASE_NAME);

            if (sourceFile.isFile()) {
                try {
                    String upLoadServerUri = "http://192.168.1.23:80/podometre/index.php";

                    // Open a url connection to the servlet
                    FileInputStream fileInputStream = new FileInputStream(sourceFile);
                    URL url = new URL(upLoadServerUri);

                    // Open a http connection to the url
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true); // Allow inputs
                    connection.setDoOutput(true); // Allow outputs
                    connection.setUseCaches(false); // Don't use a cached copy
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Connection", "Keep-Alive");
                    connection.setRequestProperty("ENCTYPE", "multipart/form-data");
                    connection.setRequestProperty("Content-Type", "multipart/form-data;boundary="
                            + boundary);
                    connection.setRequestProperty("sqlite", sourceFileUri);

                    dataOutputStream = new DataOutputStream(connection.getOutputStream());
                    dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
                    dataOutputStream.writeBytes("Content-Disposition: form-data;name=\"sqlite\";filename=\""
                            + sourceFileUri + "\"" + lineEnd);
                    dataOutputStream.writeBytes(lineEnd);

                    // create a buffer of maximum size
                    bytesAvailable = fileInputStream.available();

                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    buffer = new byte[bufferSize];

                    // read file and write it into form
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    while (bytesRead > 0) {
                        dataOutputStream.write(buffer, 0, bufferSize);
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                    }

                    // Send multipart form data necessary after file data
                    dataOutputStream.writeBytes(lineEnd);
                    dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                    // Responses from the server (code and message)
                    int serverResponseCode = connection.getResponseCode();
                    String serverResponseMessage = connection.getResponseMessage();

                    if (serverResponseCode == 200) {
                        Log.d("uploadFile", "Success > HTTP Response is : " + serverResponseMessage + " : " + serverResponseCode);
                        Toast.makeText(context.getApplicationContext(), "Success > HTTP Response is : "
                                + serverResponseMessage + " : " + serverResponseCode, Toast.LENGTH_SHORT).show();
                    }

                    // Close the stream
                    fileInputStream.close();
                    dataOutputStream.flush();
                    dataOutputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } // End of if block
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Executed";
    }

    @Override
    protected void onPostExecute(String result) {

    }

    @Override
    protected void onPreExecute() {

    }

    @Override
    protected void onProgressUpdate(Void... values) {

    }
}

package com.froura.develo4.passenger.tasks;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.froura.develo4.passenger.config.TaskConfig;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by KendrickAndrew on 01/02/2018.
 */

public class DistanceMatrixTask extends AsyncTask<Void, Void, String> {
    public static void execute(Context context) {
        new CheckUserTasks(context).execute();
    }

    public interface OnDistanceMatrixTasksListener {
        void parseDistanceMatrixJSONString(String jsonString);
        String createDistanceMatrixPostString(ContentValues contentValues) throws UnsupportedEncodingException;
    }

    private final Context context;

    public DistanceMatrixTask(Context context) {
        this.context = context;
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            // set form information

            URL url = new URL(TaskConfig.DISTANCE_MATRIX_URL);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);

            OutputStream outputStream = new BufferedOutputStream(httpURLConnection.getOutputStream());
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));

            String postString = ((CheckUserTasks.OnLoginDriverTasksListener)context).createCheckUserPostString(new ContentValues());
            bufferedWriter.write(postString);
            Log.d("distanceMatrix", postString);
            Log.d("distanceMatrix", bufferedWriter.toString());
            // clear
            bufferedWriter.flush();
            bufferedWriter.close();
            outputStream.close();

            InputStream inputStream = new BufferedInputStream(httpURLConnection.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            StringBuilder stringBuilder = new StringBuilder();
            String line = "";

            while ((line = bufferedReader.readLine()) != null)
                stringBuilder.append(line);

            // clear
            bufferedReader.close();
            inputStream.close();

            httpURLConnection.disconnect();

            return stringBuilder.toString();
        } catch (Exception e) {}
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }


    @Override
    protected void onPostExecute(String jsonString) {
        super.onPostExecute(jsonString);
        ((DistanceMatrixTask.OnDistanceMatrixTasksListener)context).parseDistanceMatrixJSONString(jsonString);
    }
}
package com.froura.develo4.passenger.tasks;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

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
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;

/**
 * Created by User on 01/10.
 */

public final class SuperTask extends AsyncTask<Void, Void, String> {

    private final Context context;
    private final String url;
    private final ProgressDialog progressDialog;
    private final String message;
    private final boolean hasDialog;
    private int resultcode;

    public SuperTask(Context context, String url, String message, boolean hasDialog) {
        this.context = context;
        this.url = url;
        this.message = message;
        this.hasDialog = hasDialog;
        progressDialog = new ProgressDialog(context);
    }

    public static void execute(Context context, String url, String message, boolean hasDialog) {
        new SuperTask(context,url,message, hasDialog).execute();
    }

    public interface TaskListener {
        void onTaskRespond(String json, int resultcode);
        ContentValues setRequestValues(ContentValues contentValues);
    }

    public static String createPostString(Set<Map.Entry<String, Object>> set) throws UnsupportedEncodingException {
        StringBuilder stringBuilder = new StringBuilder();
        boolean flag = true;

        for (Map.Entry<String, Object> value : set) {
            stringBuilder.append( flag ? "" : "&" );
            flag = false;
            stringBuilder.append(URLEncoder.encode(value.getKey(), "UTF-8"));
            stringBuilder.append("=");
            stringBuilder.append(URLEncoder.encode(value.getValue().toString(), "UTF-8"));
        }

        return stringBuilder.toString();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog.setMessage(this.message);
        progressDialog.setIndeterminate(false);
        progressDialog.setCancelable(true);
        if(hasDialog)
            progressDialog.show();
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            URL url = new URL(this.url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);

            OutputStream outputStream = new BufferedOutputStream(httpURLConnection.getOutputStream());
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
            Log.d("Error Tag", ((AppCompatActivity)context).getPackageName());
            String postString = createPostString(((TaskListener)this.context).setRequestValues(new ContentValues()).valueSet());
            bufferedWriter.write(postString);

            // clear
            bufferedWriter.flush();
            bufferedWriter.close();
            outputStream.close();

            InputStream inputStream = new BufferedInputStream(httpURLConnection.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line = "";

            while ((line = bufferedReader.readLine()) != null){
                stringBuilder.append(line);
            }

            resultcode = httpURLConnection.getResponseCode();

            // clear
            bufferedReader.close();
            inputStream.close();

            httpURLConnection.disconnect();

            Log.d("This is the return: ", stringBuilder.toString());
            return stringBuilder.toString();
        } catch (Exception ignored) {
            Log.e("Error here: ", "The error is: ", ignored);
        }
        return null;
    }

    @Override
    protected void onPostExecute(String json) {
        super.onPostExecute(json);
        progressDialog.dismiss();
        ((TaskListener)this.context).onTaskRespond(json, resultcode);
    }
}
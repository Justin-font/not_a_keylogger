package com.example.not_a_keylogger;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Trace;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements SensorEventListener {
    Button b1 ;
    Button b2 ;
    Button b3;
    Button b4 ;
    Button b5 ;
    Button b6 ;
    Button b7 ;
    Button b8 ;
    Button b9 ;
    Button bplay ;
    Button bpause ;
    private SensorManager sensorManager;
    private Sensor accelerometer;

    private List<String> buttonRecords = new ArrayList<String>();

    private List<String> accelerometerRecords= new ArrayList<String>();

    private String comment;
    private Sensor gyroscopeSensor;
    private List<String> gyroscopeRecords = new ArrayList<String>();

    float easing = 0.01F;

    float azimuth;
    float pitch;
    float roll;
    long id;
    String surface_type ="";
    String orientation_type="";
    Signature[] sigs;
    SharedPreferences sharedPreferences;
    String user_name;
    String ip="54.235.235.226";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

         b1 = (Button) findViewById(R.id.button1);
         b2 = (Button) findViewById(R.id.button2);
         b3 = (Button) findViewById(R.id.button3);
         b4 = (Button) findViewById(R.id.button4);
         b5 = (Button) findViewById(R.id.button5);
         b6 = (Button) findViewById(R.id.button6);
         b7 = (Button) findViewById(R.id.button7);
         b8 = (Button) findViewById(R.id.button8);
         b9 = (Button) findViewById(R.id.button9);
         bplay = (Button) findViewById(R.id.play);
         bpause = (Button) findViewById(R.id.pause);
        bpause.setVisibility(View.INVISIBLE);
        bplay.setVisibility(View.VISIBLE);
        sharedPreferences = this.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        if (!sharedPreferences.contains("user_name")){
            setUserName();
        }else{
            user_name = sharedPreferences.getString("user_name","");
        }

        if (sharedPreferences.contains("ip")){
            ip = sharedPreferences.getString("ip","");
        }

    }

    public void play(View v) throws PackageManager.NameNotFoundException {
        id = System.currentTimeMillis();
        bpause.setVisibility(View.VISIBLE);
        bplay.setVisibility(View.INVISIBLE);
        Toast.makeText(this, "Clicked on Button", Toast.LENGTH_LONG).show();
        record();
        // get the signature off the app (used to secure api call as a secret key)
         sigs = this.getPackageManager().getPackageInfo(this.getPackageName(), PackageManager.GET_SIGNATURES).signatures;
        for (Signature sig : sigs)
        {
            Log.i("MyApp", "Signature hashcode : " + sig.hashCode());
        }
    }
    public void changeIp(View v) throws PackageManager.NameNotFoundException {
        setIp();
    }

    public void changeUserName(View v) throws PackageManager.NameNotFoundException {

        setUserName();
    }

    public void pause(View v) throws IOException, JSONException {
        bpause.setVisibility(View.INVISIBLE);
        bplay.setVisibility(View.VISIBLE);
        sensorManager.unregisterListener(this);
        final CharSequence[] surface_items =  {"soft","hard","hand left","hand right"};
        final CharSequence[] orientation_items = {"Horizontal","45°"};
        addComment();
        surface(surface_items,"suface");
        orientation(orientation_items,"orientation");

    }
    public void send() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("accelerometerRecords", accelerometerRecords);
        json.put("buttonRecords", buttonRecords);
        json.put("gyroscopeRecords", gyroscopeRecords);
        json.put("comment", comment);
        json.put("user_name", user_name);
        json.put("surface", surface_type);
        json.put("orientation", orientation_type);
        String jsonString = json.toString();
        new CallAPI().execute(jsonString,ip);

    }

    public void save(){
        ;
        String path = String.valueOf(this.getExternalFilesDir("Records"));
        Log.i("path : ",path);

        final File file = new File(path, String.valueOf(id)+".txt");

        try
        {
            file.createNewFile();
            FileOutputStream fOut = new FileOutputStream(file);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);

            for (int i=0; i<accelerometerRecords.size(); i++) {
                myOutWriter.append("accelerometerRecord,"+accelerometerRecords.get(i)+"\n");
            }

            for (int i=0; i<gyroscopeRecords.size(); i++) {
                myOutWriter.append("gyroscopeRecord,"+gyroscopeRecords.get(i)+"\n" );
            }

            for (int i=0; i<buttonRecords.size(); i++) {
                myOutWriter.append("buttonRecord,"+buttonRecords.get(i)+"\n" );
            }
            myOutWriter.append("comment : "+comment);
            myOutWriter.append("user_name : "+user_name);
            myOutWriter.append("surface : "+surface_type);
            myOutWriter.append("orientation : "+orientation_type);
            myOutWriter.close();

            fOut.flush();
            fOut.close();
            Toast.makeText(this, "saved at : "+path, Toast.LENGTH_LONG).show();

        }

        catch (IOException e)
        {
            Log.e("Exception", "File write failed: " + e.toString());
        }

    }
    public void buttonClicked(View v){
            Button b = (Button)v;
            String buttonText = b.getText().toString();
            Long tsLong = System.currentTimeMillis();
            buttonRecords.add(tsLong +"," +buttonText);

    }


    public void record(){
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if(sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!=null){
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener((SensorEventListener) this,accelerometer,SensorManager.SENSOR_DELAY_NORMAL);

        }else{
            Toast.makeText(this,"no accelerometer :/", Toast.LENGTH_LONG).show();
        }
        if(sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)!=null){
            gyroscopeSensor =sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            sensorManager.registerListener((SensorEventListener) this,gyroscopeSensor,SensorManager.SENSOR_DELAY_NORMAL);
        }else{
            Toast.makeText(this,"no gyroscope :/", Toast.LENGTH_LONG).show();

        }

    }


    public void onSensorChanged(SensorEvent event) {
        float[] gravity = new float[3];
        float[] geomagnetic = new float[3];
        float[] I = new float[16];
        float[] R = new float[16];
        float[] orientation = new float[3];
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long tsLong = System.currentTimeMillis();
            accelerometerRecords.add(tsLong+","+ event.values[0]+","+event.values[1]+","+event.values[2]);
            //Log.i("acce value : ",String.valueOf(accelerometerRecords.size()));

            System.arraycopy(event.values,0, gravity,0,3);

            Log.i("gravity : ",String.valueOf(gravity));
        }
        else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            long tsLong = System.currentTimeMillis();
            gyroscopeRecords.add(tsLong+","+ event.values[0]+","+event.values[1]+","+event.values[2]);
           // Log.i("acce value : ",String.valueOf(gyroscopeRecords.size()));

        }

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values,0, geomagnetic,0,3);
             Log.i("geomagnetic : ",String.valueOf(geomagnetic));
        }
        if (SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
            SensorManager.getOrientation(R, orientation);
            azimuth += easing * (orientation[0] - azimuth);
            pitch += easing * (orientation[1] - pitch);
            roll += easing * (orientation[2] - roll);
            Log.i("azimuth : ",String.valueOf(azimuth));
            Log.i("pitch : ",String.valueOf(pitch));
            Log.i("roll : ",String.valueOf(roll));
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void addComment(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add comment: "+user_name);

// Set up the input
        final EditText input = new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT );
        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                comment = input.getText().toString();
                save();
                try {
                    send();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        builder.setNegativeButton("No comment", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                comment ="";
                save();
                try {
                    send();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                dialog.cancel();

            }
        });

        builder.show();
    }

    public void setUserName(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("User Name : " + user_name);

// Set up the input
        final EditText input = new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT );
        input.setText(user_name);
        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                user_name = input.getText().toString();
                sharedPreferences
                        .edit()
                        .putString("user_name", user_name)
                        .apply();
            }
        });

        builder.show();
    }

    public void setIp(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(" Ip  :" + ip);

// Set up the input
        final EditText input = new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT );
        input.setText(ip);
        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ip = input.getText().toString();
                sharedPreferences
                        .edit()
                        .putString("ip", ip)
                        .apply();
            }
        });

        builder.show();
    }
    public void surface(final CharSequence[] items, String title){
        final String[] value = {""};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setSingleChoiceItems(items , -1,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i("item", String.valueOf(which));
                        surface_type = (String) items[which];
                    }
                });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });


        AlertDialog alert = builder.create();
        alert.show();
        Log.i("value", value[0]);;
    }
    public void orientation(final CharSequence[] items, String title){
        final String[] value = {""};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setSingleChoiceItems(items , -1,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i("item", String.valueOf(which));
                        orientation_type = (String) items[which];
                    }
                });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });


        AlertDialog alert = builder.create();
        alert.show();
        Log.i("value", value[0]);;
    }
}


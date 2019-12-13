package com.example.not_a_keylogger;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
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


    }

    public void play(View v)
    {
        bpause.setVisibility(View.VISIBLE);
        bplay.setVisibility(View.INVISIBLE);
        Toast.makeText(this, "Clicked on Button", Toast.LENGTH_LONG).show();
        record();
    }

    public void pause(View v)
    {
        bpause.setVisibility(View.INVISIBLE);
        bplay.setVisibility(View.VISIBLE);
        sensorManager.unregisterListener(this);

        addComment();



    }

    public void save(){
        Long tsLong = System.currentTimeMillis();
        String path = String.valueOf(this.getExternalFilesDir("Records"));
        Log.i("path : ",path);

        final File file = new File(path, String.valueOf(tsLong)+".txt");

        try
        {
            file.createNewFile();
            FileOutputStream fOut = new FileOutputStream(file);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);

            for (int i=0; i<accelerometerRecords.size(); i++) {
                myOutWriter.append("accelerometerRecord,"+accelerometerRecords.get(i)+"\n");
            }


            for (int i=0; i<buttonRecords.size(); i++) {
                myOutWriter.append("buttonRecord,"+buttonRecords.get(i)+"\n" );
            }
            myOutWriter.append("comment : "+comment);
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

    }


    public void onSensorChanged(SensorEvent event) {
        Toast.makeText(this,"event: ", Toast.LENGTH_LONG).show();
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long tsLong = System.currentTimeMillis();
            accelerometerRecords.add(tsLong+","+ event.values[0]+","+event.values[1]+","+event.values[2]);
            Log.i("acce value : ",String.valueOf(accelerometerRecords.size()));

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void addComment(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add comment");

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
            }
        });
        builder.setNegativeButton("No comment", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                comment ="";
                save();
                dialog.cancel();

            }
        });

        builder.show();
    }

}

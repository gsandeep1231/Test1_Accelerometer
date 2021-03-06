package com.example.sandeepgangundi.test1;

import android.content.Context;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.hardware.SensorEventListener;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.hardware.Sensor;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    class sensorEvent {
        long eventTime;
        double sensorVal;
        sensorEvent()
        {
            eventTime = 0;
            sensorVal = 0.0;
        }
    }

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private long recordingStartTime;
    private long recordingEndTime;
    List<sensorEvent> sensorEventlist;
    //private SensorEventListener mSensorEventListener;
    private

    TextView xCoor;
    TextView yCoor;
    TextView zCoor;

    boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        Button start = (Button) findViewById(R.id.start);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSensorManager.registerListener(mSensorEventListener, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
                recordingStartTime = System.currentTimeMillis();
                sensorEventlist = new ArrayList<sensorEvent>();
            }
        });

        Button stop = (Button) findViewById(R.id.stop);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mSensorManager.unregisterListener(mSensorEventListener);
                    recordingEndTime = System.currentTimeMillis();
                    if(isExternalStorageWritable()) {
                        String FILENAME = "event_log_file.csv";
                        String root = Environment.getExternalStorageDirectory().toString();
                        File myDir = new File(root+"/testApp1");
                        if(myDir.mkdir() || myDir.exists())
                        {
                            File file = new File(myDir, FILENAME);
                            if (file.exists()) file.delete();
                            FileOutputStream fos = new FileOutputStream(file);
                            for (int i = 0; i < sensorEventlist.size(); i++) {
                                StringBuilder eventString = new StringBuilder();
                                eventString.append(sensorEventlist.get(i).eventTime);
                                eventString.append(",");
                                eventString.append(sensorEventlist.get(i).sensorVal);
                                eventString.append("\n");
                                fos.write(eventString.toString().getBytes());
                            }
                            fos.close();
                        }
                    }
                }
                catch(Exception E) {
                    //Need to have something here
                    E.printStackTrace();
                }

            }
        });

        xCoor = (TextView)findViewById(R.id.xcoord);
        yCoor = (TextView)findViewById(R.id.ycoord);
        zCoor = (TextView)findViewById(R.id.zcoord);

    }

    SensorEventListener mSensorEventListener = new SensorEventListener() {
        double lastReportedZ = 0.0;
        double lastReportedVal = 9.5;
        long lastReportTime = 0;
        final double sensitivity = 0.5;//(1/30G)
        final long minimumReportWindow = 500;
        boolean reportZero = false;

        @Override
        public void onSensorChanged(SensorEvent event) {
            if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];
                reportEvent(z);
                xCoor.setText("X: " + String.format("%.2f",x));
                yCoor.setText("Y: " + String.format("%.2f",y));
                zCoor.setText("Z: " + String.format("%.2f",z));
            }
        }
        public void reportEvent(float newZ) {
            long eventTime = System.currentTimeMillis() - recordingStartTime;
            if ((Math.abs(newZ - lastReportedZ) > sensitivity) && ((eventTime - lastReportTime)> minimumReportWindow)) {
                // report last status again a little in the past
                sensorEvent newMinusOneEvent = new sensorEvent();
                newMinusOneEvent.eventTime = eventTime - minimumReportWindow + (long)(minimumReportWindow/2);
                newMinusOneEvent.sensorVal = lastReportedVal;
                sensorEventlist.add(newMinusOneEvent);

                // reporting current event
                sensorEvent newEvent = new sensorEvent();
                newEvent.eventTime = eventTime;
                newEvent.sensorVal = Math.abs(newZ-lastReportedZ);
                sensorEventlist.add(newEvent);

                // tracking status for next report event
                lastReportedVal = Math.abs(newZ-lastReportedZ);
                lastReportTime = eventTime;
                lastReportedZ = newZ;
                reportZero = true;
            } else if ((Math.abs(newZ - lastReportedZ) < sensitivity) && ((eventTime - lastReportTime)> minimumReportWindow)) {
                // reporting current event as zero movement
                if (reportZero) {
                    sensorEvent newEvent = new sensorEvent();
                    newEvent.eventTime = eventTime;
                    newEvent.sensorVal = 0;
                    sensorEventlist.add(newEvent);
                    reportZero = false;
                    // tracking status for next report event
                    lastReportedVal = 0;
                    lastReportTime = eventTime;
                    lastReportedZ = newZ;
                }
            }
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

   @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

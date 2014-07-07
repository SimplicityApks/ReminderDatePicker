package com.simplicityapks.reminderdatepicker.sample;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.simplicityapks.reminderdatepicker.lib.OnDateSelectedListener;
import com.simplicityapks.reminderdatepicker.lib.ReminderDatePicker;

import java.text.DateFormat;
import java.util.Calendar;


public class MainActivity extends ActionBarActivity {

    private ReminderDatePicker datePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        datePicker = (ReminderDatePicker) findViewById(R.id.date_picker);

        // setup listener for a date change:
        datePicker.setOnDateSelectedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(Calendar date) {
                Toast.makeText(MainActivity.this, "Selected date: "+ getDateFormat().format(date.getTime()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private java.text.DateFormat savedFormat;
    public java.text.DateFormat getDateFormat() {
        if(savedFormat == null)
            savedFormat = DateFormat.getDateTimeInstance();
        return savedFormat;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }
}

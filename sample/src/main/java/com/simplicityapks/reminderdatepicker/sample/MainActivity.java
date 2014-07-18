package com.simplicityapks.reminderdatepicker.sample;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.simplicityapks.reminderdatepicker.lib.OnDateSelectedListener;
import com.simplicityapks.reminderdatepicker.lib.ReminderDatePicker;

import java.text.DateFormat;
import java.util.Calendar;

public class MainActivity extends ActionBarActivity implements CompoundButton.OnCheckedChangeListener{

    private String FLAG_DARK_THEME = "flag_dark_theme";
    private boolean useDarkTheme = false;

    private ReminderDatePicker datePicker;

    private CheckBox cbPast, cbMonth, cbNumbers, cbHideTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // do we want to change the theme to dark?
        useDarkTheme = getIntent().getBooleanExtra(FLAG_DARK_THEME, false);
        if(useDarkTheme) setTheme(R.style.Theme_AppCompat);

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

        cbPast = (CheckBox) findViewById(R.id.cb_past);
        cbMonth = (CheckBox) findViewById(R.id.cb_month);
        cbNumbers = (CheckBox) findViewById(R.id.cb_numbers);
        cbHideTime = (CheckBox) findViewById(R.id.cb_hide_time);

        // setup flag change listeners:
        cbPast.setOnCheckedChangeListener(this);
        cbMonth.setOnCheckedChangeListener(this);
        cbNumbers.setOnCheckedChangeListener(this);
        cbHideTime.setOnCheckedChangeListener(this);
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
        switch (item.getItemId()) {
            case R.id.action_switch_theme:
                Intent restart = new Intent(this, MainActivity.class);
                // add boolean extra to switch theme:
                restart.putExtra(FLAG_DARK_THEME, !useDarkTheme);
                // kill current activity and start again
                overridePendingTransition(0, 0);
                restart.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                finish();
                overridePendingTransition(0, 0);
                startActivity(restart);
                break;
            case R.id.action_view_source:
                Intent viewSource = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.uri_github_source)));
                startActivity(viewSource);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private int getCheckedFlags() {
        return  (cbPast.isChecked()? ReminderDatePicker.FLAG_PAST : 0) |
                (cbMonth.isChecked()? ReminderDatePicker.FLAG_MONTH : 0) |
                (cbNumbers.isChecked()? ReminderDatePicker.FLAG_NUMBERS : 0) |
                (cbHideTime.isChecked()? ReminderDatePicker.FLAG_HIDE_TIME : 0);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        datePicker.setFlags(getCheckedFlags());
    }
}

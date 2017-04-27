package com.youngaustralia.nimblescheduletimeclock;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    public static String timeClockId;
    public static Boolean isActive;
    public static Date startAt;
    public static String authStr;
    final DateFormat nimbleDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmz");
    final DateFormat humanDate = DateFormat.getDateTimeInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPref = this.getSharedPreferences("login", Context.MODE_PRIVATE);
        final String username = sharedPref.getString("username", null);
        final String password = sharedPref.getString("password", null);
        if (username == null || password == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
        authStr = "username="+username+"&password="+password;

        checkStatus(findViewById(R.id.btn_refresh));
    }

    public void checkStatus(View view) {
        final TextView mHeading = (TextView) findViewById(R.id.text_heading);
        final TextView mErrorView = (TextView) findViewById(R.id.txt_error);
        final TextView mStartAt = (TextView) findViewById(R.id.txt_startedat);
        final TextView mStartLabel = (TextView) findViewById(R.id.label_startat);
        final Button mClockIn = (Button) findViewById(R.id.button_clockin);
        final Button mClockOut = (Button) findViewById(R.id.button_clockout);
        final ProgressBar spinner = (ProgressBar) findViewById(R.id.spinner);
        mHeading.setVisibility(TextView.INVISIBLE);
        mStartLabel.setVisibility(TextView.INVISIBLE);
        mStartAt.setVisibility(TextView.INVISIBLE);
        mErrorView.setVisibility(TextView.INVISIBLE);
        mClockIn.setVisibility(Button.INVISIBLE);
        mClockOut.setVisibility(Button.INVISIBLE);
        spinner.setVisibility(Spinner.INVISIBLE);

        final String url = "https://app.nimbleschedule.com/api/TimeClocks/GetClockInState?format=json&" + authStr;
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        try {
            spinner.setVisibility(Spinner.VISIBLE);
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    mHeading.setVisibility(TextView.VISIBLE);
                    spinner.setVisibility(Spinner.INVISIBLE);
                    try {
                        JSONObject responseJSON = new JSONObject(response);
                        timeClockId = responseJSON.getString("TimeClockId");
                        isActive = responseJSON.getBoolean("IsActive");
                        if (isActive) {
                            mHeading.setText(getString(R.string.label_clockedin));
                            mClockOut.setVisibility(Button.VISIBLE);
                            try {
                                startAt = nimbleDate.parse(responseJSON.getString("StartAt") + "+0000");
                                mStartAt.setText(humanDate.format(startAt));
                            } catch (ParseException e) {
                                mStartAt.setText(responseJSON.getString("StartAt"));
                            }
                            mStartLabel.setVisibility(TextView.VISIBLE);
                            mStartAt.setVisibility(TextView.VISIBLE);
                        } else {
                            mHeading.setText(getString(R.string.label_clockedout));
                            mClockIn.setVisibility(Button.VISIBLE);
                        }
                    } catch (JSONException e) {
                        mHeading.setText(getString(R.string.label_error));
                        mErrorView.setVisibility(TextView.VISIBLE);
                        mErrorView.setText(response);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    mHeading.setVisibility(TextView.VISIBLE);
                    mErrorView.setVisibility(TextView.VISIBLE);
                    mErrorView.setText(error.getMessage());
                    spinner.setVisibility(Spinner.INVISIBLE);
                }
            });
            queue.add(stringRequest);
        } catch (Exception e) {
            mHeading.setVisibility(TextView.VISIBLE);
            mErrorView.setVisibility(TextView.VISIBLE);
            spinner.setVisibility(Spinner.INVISIBLE);
        }
    }

    public void clockIn(View view) {
        SharedPreferences sharedPref = this.getSharedPreferences("login", Context.MODE_PRIVATE);
        final String id = sharedPref.getString("id", null);
        final Button mClockIn = (Button) findViewById(R.id.button_clockin);
        final ProgressBar spinner = (ProgressBar) findViewById(R.id.spinner);
        mClockIn.setVisibility(TextView.INVISIBLE);
        String url = "https://app.nimbleschedule.com/api/TimeClocks/ClockIn?format=json&employeeId=" + id + "&" + authStr;
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        spinner.setVisibility(Spinner.VISIBLE);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject responseJson = new JSONObject(response);
                    checkStatus(findViewById(R.id.btn_refresh));
                } catch (JSONException e) {
                    mClockIn.setVisibility(TextView.VISIBLE);
                    TextView mHeading = (TextView) findViewById(R.id.text_heading);
                    TextView mErrorView = (TextView) findViewById(R.id.txt_error);
                    mHeading.setText(getString(R.string.label_error));
                    mHeading.setVisibility(TextView.VISIBLE);
                    mErrorView.setVisibility(TextView.VISIBLE);
                    mErrorView.setText(response);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mClockIn.setVisibility(TextView.VISIBLE);
                TextView mHeading = (TextView) findViewById(R.id.text_heading);
                TextView mErrorView = (TextView) findViewById(R.id.txt_error);
                mHeading.setText(getString(R.string.label_error));
                mHeading.setVisibility(TextView.VISIBLE);
                mErrorView.setVisibility(TextView.VISIBLE);
                spinner.setVisibility(Spinner.INVISIBLE);
            }
        });
        queue.add(stringRequest);

    }
    public void clockOut(View view) {
        SharedPreferences sharedPref = this.getSharedPreferences("login", Context.MODE_PRIVATE);
        final String id = sharedPref.getString("id", null);
        final Button mClockOut = (Button) findViewById(R.id.button_clockout);
        final ProgressBar spinner = (ProgressBar) findViewById(R.id.spinner);
        mClockOut.setVisibility(Button.INVISIBLE);
        String url = "https://app.nimbleschedule.com/api/TimeClocks/ClockOut?timeClockId=" + timeClockId + "&" + authStr;
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        spinner.setVisibility(Spinner.VISIBLE);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                checkStatus(findViewById(R.id.btn_refresh));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mClockOut.setVisibility(TextView.VISIBLE);
                TextView mHeading = (TextView) findViewById(R.id.text_heading);
                TextView mErrorView = (TextView) findViewById(R.id.txt_error);
                mHeading.setText(getString(R.string.label_error));
                mHeading.setVisibility(TextView.VISIBLE);
                mErrorView.setVisibility(TextView.VISIBLE);
                spinner.setVisibility(Spinner.INVISIBLE);
            }
        });
        queue.add(stringRequest);
    }

    public void logOut(View view) {
        // destroy credentials
        authStr = null;
        SharedPreferences sharedPref = this.getSharedPreferences("login", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("username", null);
        editor.putString("password", null);
        editor.putString("id", null);
        editor.commit(); // save immediately
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}

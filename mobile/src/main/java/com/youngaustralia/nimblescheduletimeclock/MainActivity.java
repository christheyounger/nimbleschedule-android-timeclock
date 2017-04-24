package com.youngaustralia.nimblescheduletimeclock;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.XMLReader;

import java.util.Date;
import java.util.logging.XMLFormatter;

public class MainActivity extends AppCompatActivity {

    public static String timeClockId;
    public static Boolean isActive;
    public static Date startAt;
    public static String authStr;

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
        final TextView mErrorLabel = (TextView) findViewById(R.id.label_error);
        final TextView mErrorView = (TextView) findViewById(R.id.txt_error);
        final TextView mtimeClockId = (TextView) findViewById(R.id.txt_timeclockid);
        final TextView mStartAt = (TextView) findViewById(R.id.txt_startedat);
        final TextView mIsActive = (TextView) findViewById(R.id.txt_isactive);
        final Button mClockIn = (Button) findViewById(R.id.button_clockin);
        final Button mClockOut = (Button) findViewById(R.id.button_clockout);
        mErrorLabel.setVisibility(TextView.INVISIBLE);
        mErrorView.setVisibility(TextView.INVISIBLE);
        mClockIn.setVisibility(Button.INVISIBLE);
        mClockOut.setVisibility(Button.INVISIBLE);

        final String url = "https://app.nimbleschedule.com/api/TimeClocks/GetClockInState?format=json&" + authStr;
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        try {
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject responseJSON = new JSONObject(response);
                        timeClockId = responseJSON.getString("TimeClockId");
                        mtimeClockId.setText(timeClockId);
                        mStartAt.setText(responseJSON.getString("StartAt"));
                        isActive = responseJSON.getBoolean("IsActive");
                        mIsActive.setText(responseJSON.getString("IsActive"));
                        if (isActive) {
                            mClockOut.setVisibility(Button.VISIBLE);
                        } else {
                            mClockIn.setVisibility(Button.VISIBLE);
                        }
                    } catch (JSONException e) {
                        mErrorLabel.setVisibility(TextView.VISIBLE);
                        mErrorView.setVisibility(TextView.VISIBLE);
                        mErrorView.setText(response);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    mErrorLabel.setVisibility(TextView.VISIBLE);
                    mErrorView.setVisibility(TextView.VISIBLE);
                    mErrorView.setText(error.getMessage());
                }
            });
            queue.add(stringRequest);
        } catch (Exception e) {
            mErrorLabel.setVisibility(TextView.VISIBLE);
            mErrorView.setVisibility(TextView.VISIBLE);
        }
    }

    public void clockIn(View view) {
        SharedPreferences sharedPref = this.getSharedPreferences("login", Context.MODE_PRIVATE);
        final String id = sharedPref.getString("id", null);
        final Button mClockIn = (Button) findViewById(R.id.button_clockin);
        mClockIn.setVisibility(TextView.INVISIBLE);
        String url = "https://app.nimbleschedule.com/api/TimeClocks/ClockIn?format=json&employeeId=" + id + "&" + authStr;
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject responseJson = new JSONObject(response);
                    checkStatus(findViewById(R.id.btn_refresh));
                } catch (JSONException e) {
                    mClockIn.setVisibility(TextView.VISIBLE);
                    TextView mErrorLabel = (TextView) findViewById(R.id.label_error);
                    TextView mErrorView = (TextView) findViewById(R.id.txt_error);
                    mErrorLabel.setVisibility(TextView.VISIBLE);
                    mErrorView.setVisibility(TextView.VISIBLE);
                    mErrorView.setText(response);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mClockIn.setVisibility(TextView.VISIBLE);
                TextView mErrorLabel = (TextView) findViewById(R.id.label_error);
                TextView mErrorView = (TextView) findViewById(R.id.txt_error);
                mErrorLabel.setVisibility(TextView.VISIBLE);
                mErrorView.setVisibility(TextView.VISIBLE);
            }
        });
        queue.add(stringRequest);

    }
    public void clockOut(View view) {
        SharedPreferences sharedPref = this.getSharedPreferences("login", Context.MODE_PRIVATE);
        final String id = sharedPref.getString("id", null);
        final Button mClockOut = (Button) findViewById(R.id.button_clockout);
        mClockOut.setVisibility(Button.INVISIBLE);
        String url = "https://app.nimbleschedule.com/api/TimeClocks/ClockOut?timeClockId=" + timeClockId + "&" + authStr;
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                checkStatus(findViewById(R.id.btn_refresh));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mClockOut.setVisibility(TextView.VISIBLE);
                TextView mErrorLabel = (TextView) findViewById(R.id.label_error);
                TextView mErrorView = (TextView) findViewById(R.id.txt_error);
                mErrorLabel.setVisibility(TextView.VISIBLE);
                mErrorView.setVisibility(TextView.VISIBLE);
            }
        });
        queue.add(stringRequest);
    }

    public void goToLogin(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}

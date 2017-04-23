package com.youngaustralia.nimblescheduletimeclock;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final String username = "someone";
        final String password = "test";
        final TextView mErrorView = (TextView) findViewById(R.id.txt_error);
        final TextView mtimeClockId = (TextView) findViewById(R.id.txt_timeclockid);
        final TextView mStartAt = (TextView) findViewById(R.id.txt_startedat);
        final TextView mIsActive = (TextView) findViewById(R.id.txt_isactive);
        mErrorView.setVisibility(TextView.INVISIBLE);

        String url = "https://app.nimbleschedule.com/api/TimeClocks/GetClockInState?format=json&username="+username+"&password="+password;
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        try {
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject responseJSON = new JSONObject(response);
                        mtimeClockId.setText(responseJSON.getString("TimeClockId"));
                        mStartAt.setText(responseJSON.getString("StartAt"));
                        mIsActive.setText(responseJSON.getString("IsActive"));
                    } catch (JSONException e) {
                        // ¯\_(ツ)_/¯
                        mErrorView.setVisibility(TextView.VISIBLE);
                        mErrorView.setText("That didn't work!" + response);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    mErrorView.setVisibility(TextView.VISIBLE);
                    mErrorView.setText("That didn't work!");
                }
            });
            queue.add(stringRequest);
        } catch (Exception e) {
            mErrorView.setVisibility(TextView.VISIBLE);
            mErrorView.setText("That didn't work!");
        }
    }
}

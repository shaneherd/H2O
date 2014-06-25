package com.herd.h2o.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Shane Herd on 6/19/2014.
 */

public class AddCustomer extends Activity implements View.OnClickListener {

    private EditText valveID, firstName, lastName, serviceStartDate, litersPerDay, pricePerLiter;
    private Button mSubmit;

    // Progress Dialog
    private ProgressDialog pDialog;

    // JSON parser class
    JSONParser jsonParser = new JSONParser();

    //php add a store script
    //private static final String ADD_CUSTOMER_URL = "http://10.37.152.140:1337/h2o/addcustomer.php"; //running from laptop at school
    //private static final String ADD_CUSTOMER_URL = "http://192.168.0.253:1337/h2o/addcustomer.php"; //running from laptop at home
    private static final String ADD_CUSTOMER_URL = "http://192.168.42.1/addcustomer.php"; //running on pi

    //ids
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_customer);

        valveID = (EditText)findViewById(R.id.etValveID);
        firstName = (EditText)findViewById(R.id.etFirstName);
        lastName = (EditText)findViewById(R.id.etLastName);
        serviceStartDate = (EditText)findViewById(R.id.etServiceStartDate);
        litersPerDay = (EditText)findViewById(R.id.etLitersPerDay);
        pricePerLiter = (EditText)findViewById(R.id.etPricePerLiter);

        mSubmit = (Button)findViewById(R.id.submit);
        mSubmit.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        new AddNewCustomer().execute();
    }

    class AddNewCustomer extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(AddCustomer.this);
            pDialog.setMessage("Adding Customer...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {
            // Check for success tag
            int success;
            String valve_id = valveID.getText().toString();
            String first_name = firstName.getText().toString();
            String last_name = lastName.getText().toString();
            String service_start_date = serviceStartDate.getText().toString();
            String liters_per_day = litersPerDay.getText().toString();
            String price_per_liter = pricePerLiter.getText().toString();

            try {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("valveID", valve_id));
                params.add(new BasicNameValuePair("firstName", first_name));
                params.add(new BasicNameValuePair("lastName", last_name));
                params.add(new BasicNameValuePair("serviceStartDate", service_start_date));
                params.add(new BasicNameValuePair("litersPerDay", liters_per_day));
                params.add(new BasicNameValuePair("pricePerLiter", price_per_liter));

                Log.d("request!", "starting");

                //Posting user data to script
                JSONObject json = jsonParser.makeHttpRequest(
                        ADD_CUSTOMER_URL, "POST", params);

                // full json response
                Log.d("Add Customer Attempt", json.toString());

                // json success element
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("Customer Added!", json.toString());
                    finish();
                    return json.getString(TAG_MESSAGE);
                }else{
                    Log.d("Add Customer Failure!", json.getString(TAG_MESSAGE));
                    return json.getString(TAG_MESSAGE);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(String file_url) {
            // dismiss the dialog once product deleted
            pDialog.dismiss();
            if (file_url != null){
                Toast.makeText(AddCustomer.this, file_url, Toast.LENGTH_LONG).show();
            }
        }
    }
}

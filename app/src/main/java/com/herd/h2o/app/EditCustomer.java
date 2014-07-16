package com.herd.h2o.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
 * Created by Shane Herd on 7/14/2014.
 */

public class EditCustomer extends Activity {
    private String idString, valveIDString, firstNameString, lastNameString, serviceStartDateString, litersPerDayString, pricePerLiterString = "";

    private EditText valveID, firstName, lastName, serviceStartDate, litersPerDay, pricePerLiter;
    private Button mSubmit;

    // Progress Dialog
    private ProgressDialog pDialog;

    // JSON parser class
    JSONParser jsonParser = new JSONParser();

    //php update customer script
    private static final String UPDATE_CUSTOMER_URL = "http://192.168.42.1/updatecustomer.php"; //running on pi
    private static final String DELETE_CUSTOMER_URL = "http://192.168.42.1/deletecustomer.php"; //running on pi

    //ids
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_customer);

        Bundle extras = getIntent().getExtras();  //this is the extra stuff that was passed from the previous activity
        if (extras != null) {
            idString = extras.getString("customerID");
            firstNameString = extras.getString("firstName");
            lastNameString = extras.getString("lastName");
            valveIDString = extras.getString("valveID");
            serviceStartDateString = extras.getString("serviceStartDate");
            litersPerDayString = extras.getString("litersPerDay");
            pricePerLiterString = extras.getString("pricePerLiter");
        }

        valveID = (EditText)findViewById(R.id.etValveID);
        firstName = (EditText)findViewById(R.id.etFirstName);
        lastName = (EditText)findViewById(R.id.etLastName);
        serviceStartDate = (EditText)findViewById(R.id.etServiceStartDate);
        litersPerDay = (EditText)findViewById(R.id.etLitersPerDay);
        pricePerLiter = (EditText)findViewById(R.id.etPricePerLiter);

        valveID.setText(valveIDString);
        firstName.setText(firstNameString);
        lastName.setText(lastNameString);
        serviceStartDate.setText(serviceStartDateString);
        litersPerDay.setText(litersPerDayString);
        pricePerLiter.setText(pricePerLiterString);

        mSubmit = (Button)findViewById(R.id.submit);
        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                new EditTheCustomer().execute();
            }
        });

        //Delete Feature
        final AlertDialog.Builder deleteDialog = new AlertDialog.Builder(this)
                .setTitle("Delete")
                .setMessage("Are you sure you want to delete this customer?\nThis action can not be undone.")
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        new DeleteCustomer().execute();//continue with the delete
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        // do nothing
                    }
                });

        Button btnDelete = (Button)findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //prompt the user to confirm cancellation
                //warn that all data entered will be lost
                deleteDialog.show(); //prompt the user to confirm deletion
            }
        });
    }

    class EditTheCustomer extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(EditCustomer.this);
            pDialog.setMessage("Editing Customer...");
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
                params.add(new BasicNameValuePair("id", idString));
                params.add(new BasicNameValuePair("valveID", valve_id));
                params.add(new BasicNameValuePair("firstName", first_name));
                params.add(new BasicNameValuePair("lastName", last_name));
                params.add(new BasicNameValuePair("serviceStartDate", service_start_date));
                params.add(new BasicNameValuePair("litersPerDay", liters_per_day));
                params.add(new BasicNameValuePair("pricePerLiter", price_per_liter));
                params.add(new BasicNameValuePair("oldValveID", valveIDString));

                Log.d("request!", "starting");

                //Posting user data to script
                JSONObject json = jsonParser.makeHttpRequest(
                        UPDATE_CUSTOMER_URL, "POST", params);

                // full json response
                Log.d("Edit Customer Attempt", json.toString());

                // json success element
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("Customer Edited!", json.toString());
                    finish();
                    return json.getString(TAG_MESSAGE);
                }else{
                    Log.d("Edit Customer Failure!", json.getString(TAG_MESSAGE));
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
                Toast.makeText(EditCustomer.this, file_url, Toast.LENGTH_LONG).show();
            }
        }
    }

    class DeleteCustomer extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(EditCustomer.this);
            pDialog.setMessage("Deleting Customer...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {
            // Check for success tag
            int success;

            try {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("id", idString));
                params.add(new BasicNameValuePair("valveID", valveIDString));

                Log.d("request!", "starting");

                //Posting user data to script
                JSONObject json = jsonParser.makeHttpRequest(
                        DELETE_CUSTOMER_URL, "POST", params);

                // full json response
                Log.d("Delete Customer Attempt", json.toString());

                // json success element
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("Customer Deleted!", json.toString());
                    finish();
                    return json.getString(TAG_MESSAGE);
                }else{
                    Log.d("Delete Customer Failure!", json.getString(TAG_MESSAGE));
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
                Toast.makeText(EditCustomer.this, file_url, Toast.LENGTH_LONG).show();
            }
        }
    }
}

